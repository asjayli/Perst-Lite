package org.garret.perst.impl;

import java.io.UnsupportedEncodingException;
import org.garret.perst.StorageError;
import java.io.*;

public class ByteBuffer {
    public final void extend(int size) {  
        if (size > arr.length) { 
            int newLen = size > arr.length*2 ? size : arr.length*2;
            byte[] newArr = new byte[newLen];
            System.arraycopy(arr, 0, newArr, 0, used); 
            arr = newArr;
        }
        used = size;
    }
    
    final byte[] toArray() { 
        byte[] result = new byte[used];
        System.arraycopy(arr, 0, result, 0, used); 
        return result;
    }

    final int size() { 
        return used;
    }

    int packString(int dst, String value, String encoding) { 
        if (value == null) { 
            extend(dst + 4);
            Bytes.pack4(arr, dst, -1);
            dst += 4;        
        } else { 
            int length = value.length();
            if (encoding == null) { 
                extend(dst + 4 + 2*length);
                Bytes.pack4(arr, dst, length);
                dst += 4;
                for (int i = 0; i < length; i++) { 
                    Bytes.pack2(arr, dst, (short)value.charAt(i));
                    dst += 2;
                }
            } else { 
                try { 
                    byte[] bytes = value.getBytes(encoding);
                    extend(dst + 4 + bytes.length);
                    Bytes.pack4(arr, dst, -2-bytes.length);
                    System.arraycopy(bytes, 0, arr, dst+4, bytes.length);
                    dst += 4 + bytes.length;
                } catch (UnsupportedEncodingException x) { 
                    throw new StorageError(StorageError.UNSUPPORTED_ENCODING);
                }
            }        
        }
        return dst;
    }
    
    class ByteBufferOutputStream extends OutputStream { 
        public void write(int b) {
            write(new byte[]{(byte)b}, 0, 1);
        }

        public void write(byte b[], int off, int len) {
            int pos = used;
            extend(pos + len);
            System.arraycopy(b, off, arr, pos, len);
        }
    }

    public OutputStream getOutputStream() { 
        return new ByteBufferOutputStream();
    }

    ByteBuffer() { 
        arr = new byte[64];
    }

    public byte[] arr;
    public int    used;
}




