package se.kth.castor.instrumentation.plugins;

import org.glowroot.agent.plugin.api.*;
import org.glowroot.agent.plugin.api.weaving.*;

import java.io.BufferedWriter;
import java.io.FileWriter;

public class PDFBoxAspect6 {
    private static int INVOCATION_COUNT;
    @Pointcut(className = "org.apache.pdfbox.debugger.flagbitspane.FieldFlag",
            methodName = "isFlagBitSet",
            methodParameterTypes = {"int", "int"},
            timerName = "org.apache.pdfbox.debugger.flagbitspane.FieldFlag-isFlagBitSet")
    public static class PureMethodAdvice implements AdviceTemplate {
        private static final TimerName timer = Agent.getTimerName(PureMethodAdvice.class);
        private static final String transactionType = "Pure";
        private static final int COUNT = 6;
        private static String receivingObjectFilePath;
        private static String paramObjectsFilePath;
        private static String returnedObjectFilePath;
        private static String invocationCountFilePath;
        private static Logger logger = Logger.getLogger(PureMethodAdvice.class);
        private static final String methodFQN = PureMethodAdvice.class.getAnnotation(Pointcut.class).className() + "."
                + PureMethodAdvice.class.getAnnotation(Pointcut.class).methodName();

        private static void setup() {
            AdviceTemplate.setUpXStream();
            String[] fileNames = AdviceTemplate.setUpFiles(methodFQN);
            receivingObjectFilePath = fileNames[0];
            paramObjectsFilePath = fileNames[1];
            returnedObjectFilePath = fileNames[2];
            invocationCountFilePath = fileNames[3];
        }

        public static synchronized void writeObjectXMLToFile(Object objectToWrite, String objectFilePath) {
            try {
                FileWriter objectFileWriter = new FileWriter(objectFilePath, true);
                xStream.toXML(objectToWrite, objectFileWriter);
                BufferedWriter bw = new BufferedWriter(objectFileWriter);
                bw.newLine();
                bw.flush();
                bw.close();
            } catch (Exception e) {
                logger.info("PDFBoxAspect" + COUNT);
            }
        }

        public static synchronized void writeInvocationCountToFile() {
            try {
                FileWriter objectFileWriter = new FileWriter(invocationCountFilePath);
                objectFileWriter.write("Invocation count for " + methodFQN + " : " + INVOCATION_COUNT);
                objectFileWriter.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @OnBefore
        public static TraceEntry onBefore(OptionalThreadContext context,
                                          @BindReceiver Object receivingObject,
                                          @BindParameterArray Object parameterObjects,
                                          @BindMethodName String methodName) {
            if (INVOCATION_COUNT < 50) {
                setup();
                writeObjectXMLToFile(receivingObject, receivingObjectFilePath);
                writeObjectXMLToFile(parameterObjects, paramObjectsFilePath);
            }
            MessageSupplier messageSupplier = MessageSupplier.create(
                    "className: {}, methodName: {}",
                    PureMethodAdvice.class.getAnnotation(Pointcut.class).className(),
                    methodName
            );
            return context.startTransaction(transactionType, methodName, messageSupplier, timer, OptionalThreadContext.AlreadyInTransactionBehavior.CAPTURE_NEW_TRANSACTION);
        }

        @OnReturn
        public static void onReturn(@BindReturn Object returnedObject,
                                    @BindTraveler TraceEntry traceEntry) {
            if (INVOCATION_COUNT < 50) {
                writeObjectXMLToFile(returnedObject, returnedObjectFilePath);
            }
            INVOCATION_COUNT++;
            writeInvocationCountToFile();
            traceEntry.end();
        }

        @OnThrow
        public static void onThrow(@BindThrowable Throwable throwable,
                                   @BindTraveler TraceEntry traceEntry) {
            traceEntry.endWithError(throwable);
        }
    }
}
