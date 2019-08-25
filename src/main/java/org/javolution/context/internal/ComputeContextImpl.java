/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.context.internal;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.javolution.context.ComputeContext;
import static org.javolution.context.LogContext.*;
import org.javolution.util.FastMap;
import org.javolution.util.FastTable;
import org.bridj.Pointer;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLDevice;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLMem;
import com.nativelibs4java.opencl.CLPlatform.DeviceFeature;
import com.nativelibs4java.opencl.CLProgram;
import com.nativelibs4java.opencl.CLQueue;
import com.nativelibs4java.opencl.JavaCL;

/**
 * Holds the default implementation of compute context.
 */
public final class ComputeContextImpl extends ComputeContext {

	/**
	 * Buffer implementation.
	 */
	class BufferImpl implements ComputeContext.Buffer {
		/** Read-only view over a buffer */
		class ReadOnly implements Buffer {
			@Override
			public ByteBuffer asByteBuffer() {
				return BufferImpl.this.asByteBuffer().asReadOnlyBuffer();
			}

			@Override
			public CharBuffer asCharBuffer() {
				return asByteBuffer().asCharBuffer();
			}

			@Override
			public DoubleBuffer asDoubleBuffer() {
				if (!ComputeContext.DOUBLE_PRECISION_REQUIRED.get())
					throw new UnsupportedOperationException(
							"ComputeContext#DOUBLE_PRECISION_REQUIRED disabled");
				return asByteBuffer().asDoubleBuffer();
			}

			@Override
			public FloatBuffer asFloatBuffer() {
				return asByteBuffer().asFloatBuffer();
			}

			@Override
			public IntBuffer asIntBuffer() {
				return asByteBuffer().asIntBuffer();
			}

			@Override
			public LongBuffer asLongBuffer() {
				return asByteBuffer().asLongBuffer();
			}

			@Override
			public ShortBuffer asShortBuffer() {
				return asByteBuffer().asShortBuffer();
			}

			BufferImpl buffer() {
				return BufferImpl.this;
			}

			@Override
			public void export() {
				buffer().export();
			}

			@Override
			public long getByteCount() {
				return buffer().getByteCount();
			}

			@Override
			public Buffer readOnly() {
				return this;
			}
		}
		ByteBuffer byteBuffer; // Host buffer (null until exported).
		CLBuffer<Byte> clBuffer; // Device buffer (null when on host)
		CLEvent updateEvent; // Last event which has updated clBuffer.

		BufferImpl(java.nio.Buffer init) {
			clBuffer = clContext.createByteBuffer(CLMem.Usage.InputOutput,
					init, true);
		}

		BufferImpl(long byteCount) {
			clBuffer = clContext.createByteBuffer(CLMem.Usage.InputOutput,
					byteCount);
		}

		@Override
		public ByteBuffer asByteBuffer() {
			export();
			return byteBuffer;
		}

		@Override
		public CharBuffer asCharBuffer() {
			return asByteBuffer().asCharBuffer();
		}

		@Override
		public DoubleBuffer asDoubleBuffer() {
			if (!ComputeContext.DOUBLE_PRECISION_REQUIRED.get())
				throw new UnsupportedOperationException(
						"ComputeContext#DOUBLE_PRECISION_REQUIRED disabled");
			return asByteBuffer().asDoubleBuffer();
		}

		@Override
		public FloatBuffer asFloatBuffer() {
			return asByteBuffer().asFloatBuffer();
		}

		@Override
		public IntBuffer asIntBuffer() {
			return asByteBuffer().asIntBuffer();
		}

		@Override
		public LongBuffer asLongBuffer() {
			return asByteBuffer().asLongBuffer();
		}

		@Override
		public ShortBuffer asShortBuffer() {
			return asByteBuffer().asShortBuffer();
		}

		/** Copies this buffer to device memory. */
		void copyToDevice() {
			if (clBuffer != null)
				return; // Already on device.
			clBuffer = clContext.createByteBuffer(CLMem.Usage.InputOutput,
					byteBuffer, true /* copy */);
		}

		/** Moves this buffer to host memory and release device buffer. */
		@SuppressWarnings("unchecked")
		@Override
		public void export() {
			if (byteBuffer != null)
				return; // Already on host.
			if (clBuffer == null)
				throw new UnsupportedOperationException(
						"The device buffer has already been released.");
			if (getByteCount() > Integer.MAX_VALUE)
				throw new UnsupportedOperationException(
						"Buffer byte count exceeds java.nio.ByteBuffer maximum capacity");
			byteBuffer = ByteBuffer.allocateDirect((int) getByteCount()).order(
					clQueue.getDevice().getByteOrder());
			clBuffer.read(clQueue,
					(Pointer<Byte>) Pointer.pointerToBuffer(byteBuffer),
					true /* blocking */, updateEvent);
			release();
		}

		@Override
		public long getByteCount() {
			return (clBuffer != null) ? clBuffer.getByteCount() : byteBuffer
					.capacity();
		}

		@Override
		public Buffer readOnly() {
			return new ReadOnly();
		}

		void release() {
			if (clBuffer != null)
				clBuffer.release();
			clBuffer = null;
			if (updateEvent != null)
				updateEvent.release();
			updateEvent = null;
		}
	}

	/** Kernel implementation. */
	class KernelImpl implements Kernel {
		CLKernel clKernel;
		int[] globalWorkSize;
		int[] localWorkSize;
		FastTable<BufferImpl> updatedBuffers = new FastTable<BufferImpl>();
		FastTable<CLEvent> dependencies = new FastTable<CLEvent>();

		KernelImpl(CLKernel clKernel) {
			this.clKernel = clKernel;
		}

		@Override
		public void execute() {
			CLEvent updateEvt = clKernel.enqueueNDRange(clQueue,
					globalWorkSize, localWorkSize,
					dependencies.toArray(new CLEvent[0]));
			for (BufferImpl buffer : updatedBuffers) {
				buffer.updateEvent = updateEvt;
			}
		}

		@Override
		public void setArguments(Object... args) {
			int i = 0;
			for (Object arg : args) {
				if (arg instanceof Buffer) {
					BufferImpl buffer;
					if (arg instanceof BufferImpl.ReadOnly) {
						buffer = ((BufferImpl.ReadOnly) arg).buffer();
					} else {
						buffer = ((BufferImpl) arg);
						updatedBuffers.add(buffer);
					}
					if (buffer.updateEvent != null)
						dependencies.add(buffer.updateEvent);
					buffer.copyToDevice();
					clKernel.setArg(i++, buffer.clBuffer);
				} else if (arg instanceof Boolean) {
					clKernel.setArg(i++, (Boolean) arg);
				} else if (arg instanceof Byte) {
					clKernel.setArg(i++, (Byte) arg);
				} else if (arg instanceof Short) {
					clKernel.setArg(i++, (Short) arg);
				} else if (arg instanceof Integer) {
					clKernel.setArg(i++, (Integer) arg);
				} else if (arg instanceof Long) {
					clKernel.setArg(i++, (Long) arg);
				} else if (arg instanceof Float) {
					clKernel.setArg(i++, (Float) arg);
				} else if (arg instanceof Double) {
					clKernel.setArg(i++, (Double) arg);
				} else {
					throw new IllegalArgumentException("Argument " + arg
							+ " is not a Buffer or a primitive type supported.");
				}
			}
		}

		@Override
		public void setGlobalWorkSize(int... gws) {
			globalWorkSize = gws;
		}

		@Override
		public void setLocalWorkSize(int... lws) {
			localWorkSize = lws;
		}
	}
	/** Program implementation. */
	class ProgramImpl {
		CLProgram clProgram;
		FastMap<String, CLKernel> kernels = new FastMap<String, CLKernel>();

		ProgramImpl(String opencl) {
			clProgram = clContext.createProgram(PRAGMA_DOUBLE_SUPPORT + opencl);
			CLKernel[] clKernels = clProgram.createKernels();
			for (CLKernel clKernel : clKernels) {
				kernels.put(clKernel.getFunctionName(), clKernel);
			}
		}

		void release() {
			for (CLKernel kernel : kernels.values()) {
				kernel.release();
			}
			clProgram.release();
		}
	}

	// Support for double floating-point precision is a requirement
	// for scientific computing algorithms/applications.
	private static final String PRAGMA_DOUBLE_SUPPORT = "#pragma OPENCL EXTENSION cl_khr_fp64 : enable\n";

	// In this (basic) implementation we use a single context and
	// a single queue for all ComputeContext instances.
	private static CLContext clContext;
	private static CLQueue clQueue;
	static {
		// Avoid polluting display with info messages.
		Logger.getLogger("org.bridj").setLevel(Level.WARNING);
		try {
			 clContext = createContext();
			 clQueue = clContext.createDefaultQueue();
		} catch (Throwable error) {
			warning("OpenCL drivers not installed; ComputeContext disabled!");
		}
	}

	// Each context instance has its own local programs and buffers.
	private final ComputeContextImpl parent;
	private final FastMap<Class<? extends Program>, ProgramImpl> programs = new FastMap<Class<? extends Program>, ProgramImpl>();
	private final FastTable<BufferImpl> buffers = new FastTable<BufferImpl>();

	public ComputeContextImpl() {
		parent = null;
	}

	public ComputeContextImpl(ComputeContextImpl parent) {
		this.parent = parent;
	}

	@Override
	protected BufferImpl createBuffer(java.nio.Buffer init) {
		BufferImpl buffer = new BufferImpl(init);
		if (parent != null) { // Keep buffer only if it can be released upon exit.
			buffers.add(buffer);
		}
		return buffer;
	}

	@Override
	protected BufferImpl createBuffer(long byteCount) {
		checkOpenCL();
		BufferImpl buffer = new BufferImpl(byteCount);
		if (parent != null) { // Keep buffer only if it can be released upon exit.
			buffers.add(buffer);
		}
		return buffer;
	}

	@Override
	protected KernelImpl createKernel(Class<? extends Program> programClass,
			String kernelName) {
		checkOpenCL();
		ProgramImpl program = searchProgram(programClass);
		if (program == null) { // Program not found, instantiate the class.
			loadAndBuild(newInstance(programClass));
			program = programs.get(programClass);
		}
		CLKernel clKernel = program.kernels.get(kernelName);
		if (clKernel == null)
			throw new IllegalArgumentException("Kernel " + kernelName
					+ " not defined in " + programClass.getName());
		return new KernelImpl(clKernel);
	}

	/* Release all devices resources allocated while in this context */
	@Override
	public void exit() {
		for (BufferImpl buffer : buffers) {
			buffer.release();
		}
		for (ProgramImpl program : programs.values()) {
			program.release();
		}
		super.exit();
	}

	@Override
	protected ComputeContextImpl inner() {
		return new ComputeContextImpl(this);
	}

	@Override
	protected void loadAndBuild(Program program) {
		checkOpenCL();
		if (searchProgram(program.getClass()) != null)
			return; // Exist already.
		ProgramImpl prg = new ProgramImpl(program.toOpenCL());
		programs.put(program.getClass(), prg);
	}

	private ProgramImpl searchProgram(Class<? extends Program> cls) {
		ProgramImpl program = programs.get(cls);
		if (program != null)
			return program;
		if (parent != null)
			return parent.searchProgram(cls);
		return null;
	}

	@Override
	protected void unloadAndFree(Program program) {
		checkOpenCL();
		ProgramImpl prg = programs.remove(program.getClass());
		if (prg != null) {
			prg.release();
		}
	}

	private static void checkOpenCL() {
		if (clQueue == null) throw new UnsupportedOperationException(
				"No OpenCL Driver Installed.");
	}

	private static CLContext createContext() {
		final boolean doublePrecisionRequired = ComputeContext.DOUBLE_PRECISION_REQUIRED
				.get();
		CLContext context = doublePrecisionRequired ? JavaCL.createBestContext(
				DeviceFeature.DoubleSupport, DeviceFeature.GPU,
				DeviceFeature.MaxComputeUnits) : JavaCL.createBestContext(
				DeviceFeature.GPU, DeviceFeature.MaxComputeUnits);
		CLDevice[] devices = context.getDevices();
		for (CLDevice device : devices) {
			if (doublePrecisionRequired) {
				info("ComputeContext device (double precision support required): ",
								device);
			} else {
				info("ComputeContext device (double precision support not required): ",
								device);
			}
		}
		return context;
	}

	private static Program newInstance(Class<? extends Program> programClass) {
		try {
			return programClass.newInstance();
		} catch (InstantiationException e) {
			error(e);
			throw new InstantiationError(e.getMessage());
		} catch (IllegalAccessException e) {
			error(e);
			throw new IllegalAccessError(e.getMessage());
		}
	}


}
