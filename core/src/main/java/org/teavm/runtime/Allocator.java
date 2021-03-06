/*
 *  Copyright 2016 Alexey Andreev.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.teavm.runtime;

import org.teavm.interop.Address;
import org.teavm.interop.NoGC;
import org.teavm.interop.StaticInit;
import org.teavm.interop.Structure;

@StaticInit
public final class Allocator {
    private Allocator() {
    }

    public static Address allocate(RuntimeClass tag) {
        RuntimeObject object = GC.alloc(tag.size);
        fillZero(object.toAddress(), tag.size);
        object.classReference = tag.toAddress().toInt() >> 3;
        return object.toAddress();
    }

    public static Address allocateArray(RuntimeClass tag, int size) {
        int itemSize = (tag.itemType.flags & RuntimeClass.PRIMITIVE) != 0 ? tag.itemType.size : 4;
        int sizeInBytes = Address.align(Address.fromInt(Structure.sizeOf(RuntimeArray.class)), itemSize).toInt();
        sizeInBytes += itemSize * size;
        sizeInBytes = Address.align(Address.fromInt(sizeInBytes), 4).toInt();
        Address result = GC.alloc(sizeInBytes).toAddress();
        fillZero(result, sizeInBytes);

        RuntimeArray array = result.toStructure();
        array.classReference = tag.toAddress().toInt() >> 3;
        array.size = size;

        return result;
    }

    @NoGC
    public static native void fillZero(Address address, int count);

    @NoGC
    public static native void moveMemoryBlock(Address source, Address target, int count);

    @NoGC
    public static native boolean isInitialized(Class<?> cls);
}
