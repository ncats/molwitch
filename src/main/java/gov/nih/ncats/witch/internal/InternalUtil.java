package gov.nih.ncats.witch.internal;

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
