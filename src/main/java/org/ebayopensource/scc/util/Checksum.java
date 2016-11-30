/*******************************************************************************
 * Copyright (c) 2016 eBay Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.ebayopensource.scc.util;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.UUID;

/**
 * checksum routines to compute checksum for binary data.
 * 
 * the checksum is based on UUID variant 2, version 3. the strength of the check sum
 * is very close to md5 hash. (6 bits less). additional logic are added to the 
 * UUID algorithm to 
 *   - convert to a custom base 32 string to allow safe embedding checksum into an URL
 *   - 5 bits of additional integrity information based on Java string hashCode()
 * 
 * The last two chars of the checksum only has 256 variations. This allows it to be 
 * used as a directory name.
 * 
 */
public final class Checksum {

   /**
    * compute the checksum of byte array
    * 
    * @param data
    * @return
    */
   public static String checksum(byte[] data) {
      UUID uuid = UUID.nameUUIDFromBytes(data);

      ByteBuffer buffer = ByteBuffer.allocate(16);
      buffer.putLong(uuid.getMostSignificantBits());
      buffer.putLong(uuid.getLeastSignificantBits());
      String encoded = encode(buffer.array());
      return encoded + hash(encoded);
   }

   /**
    * internal method to compute 5 bits hash code from a string
    * 
    * @param enc
    * @return
    */
   private static char hash(String enc) {
      int index = enc.hashCode() % 32;
      if (index < 0) {
         return alphabet[-index];
      } else {
         return alphabet[index];
      }
   }

   /**
    * compute the checksum of an input string
    * 
    * @param in
    * @return
    */
   public static String checksum(String in) {
      try {
         return checksum(in.getBytes("UTF-8"));
      } catch (UnsupportedEncodingException e) {
         throw new RuntimeException(e);
      }
   }

   /**
    * custom base 32 encoder. the encoder is different from standard 
    * base 32 encoder such that
    *   - all letters are in lower case
    *   - use number 0-6, as oppose to 2-8
    * 
    * @param data
    * @return
    */
   protected static String encode(byte[] data) {
      int index = 0;
      int digit = 0;
      int currByte;
      int nextByte;

      StringBuilder base32 = new StringBuilder((data.length * 8) / 5 + 1);
      int count = data.length;

      int i = 0;
      while (i < count) {
         currByte = (data[i] >= 0) ? data[i] : (data[i] + 256);

         if (index > 3) {
            if ((i + 1) < data.length) {
               nextByte = (data[i + 1] >= 0) ? data[i + 1] : (data[i + 1] + 256);
            } else {
               nextByte = 0;
            }

            digit = currByte & (0xFF >> index);
            index = (index + 5) % 8;
            digit <<= index;
            digit |= nextByte >> (8 - index);
            i++;
         } else {
            digit = (currByte >> (8 - (index + 5))) & 0x1F;
            index = (index + 5) % 8;
            if (index == 0) {
               i++;
            }
         }
         base32.append(alphabet[digit]);
      }

      return base32.toString();
   }

   //
   // coding characters
   //
   final private static char[] alphabet = new char[32];

   final private static byte[] reverseMap = new byte[128];
   static {
      Arrays.fill(reverseMap, (byte) 0xFF);
      for (int i = 'a'; i <= 'z'; i++) {
         alphabet[i - 'a'] = (char) i;
         reverseMap[i] = (byte) (i - 'a');
      }

      for (int i = 0; i < 6; ++i) {
         alphabet[26 + i] = (char) (i + '0');
         reverseMap[i + '0'] = (byte) (26 + i);
      }
   }
}
