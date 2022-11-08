import service.MetricService;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class Console {

    private final MetricService metricService;

    public Console() throws IOException {
        Properties prop = new Properties();

        InputStream inputStream = ConsoleRunner.class.getClassLoader()
                .getResourceAsStream("console.properties");
        prop.load(inputStream);

        metricService = new MetricService(prop.getProperty("gcp.domain"), prop.getProperty("gcp.project"));
    }

    public static void usage() {
        System.out.println("Usage:");
        System.out.println();
        System.out.println("  list-log-metrics            | Lists of log metrics");
        System.out.println("  new-log-metrics             | Creates a log metrics");
        System.out.println("  delete-log-metrics          | Deletes a log metrics");
        System.out.println("  exit                        | Exit from console");
        System.out.println();
    }

    /**
     * Handles a single command.
     *
     * @param commandLine A line of input provided by the user
     */
    public void handleCommandLine(String commandLine) throws IOException {
        String[] args = commandLine.split("\\s+");

        if (args.length < 1) {
            throw new IllegalArgumentException("not enough args");
        }

        String command = args[0];
        switch (command) {
            case "list-log-metrics":
                args = commandLine.split("\\s+", 2);
                if (args.length != 1) {
                    throw new IllegalArgumentException("usage: no arguments");
                }
                metricService.listLogMetrics();
                break;
            case "new-log-metrics":
                args = commandLine.split("\\s+", 4);
                if (args.length != 4) {
                    throw new IllegalArgumentException("usage: <type>");
                }
                metricService.createLogMetrics(args[1], args[2], args[3]);
                break;
            case "delete-log-metrics":
                args = commandLine.split("\\s+", 2);
                if (args.length != 2) {
                    throw new IllegalArgumentException("usage: <type>");
                }
                metricService.deleteLogMetrics(args[1]);
                break;
            case "exit":
                System.out.println("exiting...");
                System.exit(0);
                break;
            default:
                throw new IllegalArgumentException("unrecognized command: " + command);
        }
    }
}
