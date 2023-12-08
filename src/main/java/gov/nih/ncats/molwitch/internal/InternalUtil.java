/*
 * NCATS-MOLWITCH
 *
 * Copyright 2023 NIH/NCATS
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package gov.nih.ncats.molwitch.internal;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by katzelda on 7/19/16.
 */
public class InternalUtil {


    private static AtomicBoolean debug = new AtomicBoolean(false);


//    public static void printDebug( Runnable code){
//        debug.set(true);
//        try{
//            code.run();
//        }finally{
//            debug.set(false);
//        }
//    }

    public static void logDebug(Runnable r){
        if(debug.get()){
            r.run();
        }
    }

    public static <E extends Exception> void printDebug( RunnableCode<E> code) throws E{
        debug.set(true);
        try{
            code.run();
        }finally{
            debug.set(false);
        }
    }

    public static <T, E extends Exception> T printDebug( CallableCode<T,E> code) throws E{
        debug.set(true);
        try{
            return code.call();
        }finally{
            debug.set(false);
        }
    }

    public interface CallableCode<R, E extends Exception> extends Callable<R> {
        R call() throws E;
    }

    public interface RunnableCode<E extends Exception>{
        void run() throws E;
    }
    
    public static void toggle() {
    		boolean temp;
          do {
              temp = debug.get();
          } while(!debug.compareAndSet(temp, !temp));
    }
    
    public static void on() {
    		debug.set(true);
    }
    
    public static void off() {
		debug.set(false);
}
}
