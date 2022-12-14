/*
 * Apache License, Version 2.0
 * 
 * Copyright 2013-2022 Sly Technologies Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 *   
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jnetpcap.internal;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryAddress;
import java.lang.foreign.MemorySession;
import java.lang.invoke.MethodHandle;

/**
 * @author Sly Technologies Inc
 * @author repos@slytechs.com
 * @author mark
 *
 */
public class ForeignUpcall<T> {

	private static final Linker C_LINKER = Linker.nativeLinker();

	private final MemoryAddress address;
	private final MemorySession scope;
	private final String message; // Stub error handler
	private final Throwable cause; // Stub error handler

	public ForeignUpcall(MethodHandle handle, FunctionDescriptor descriptor, MemorySession scope) {
		this.scope = scope;
		this.address = C_LINKER.upcallStub(handle, descriptor, scope)
				.address();
		this.message = null;
		this.cause = null;
	}

	public ForeignUpcall(String message, Throwable cause) {
		this.message = message;
		this.cause = cause;
		this.scope = null;
		this.address = null;
	}

	private MemoryAddress checkedAddress() {
		if (address == null)
			throw (cause instanceof RuntimeException e)
					? e
					: new RuntimeException(message, cause);

		return address;
	}

	public MemoryAddress bindTo(T target) {
		throw new UnsupportedOperationException();
	}

	public MemoryAddress address() {
		return checkedAddress();
	}

	public MemorySession scope() {
		return scope;
	}
}
