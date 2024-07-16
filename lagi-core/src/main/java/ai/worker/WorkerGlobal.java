package ai.worker;


public class WorkerGlobal {
    public static String USER_ROLE = "user";
    public static double TEMPERATURE = 0.8;
    public static boolean STREAM = false;
    public static int MAX_TOKENS = 400;

    public static String ROBOT_WORKER_CLASS = "ai.worker.social.RobotWorker";
    public static String ASR_FLIGHT_WORKER_CLASS = "ai.worker.audio.Asr4Flights";
}
