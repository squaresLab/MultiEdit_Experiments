package util;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;

import java.io.*;
import java.util.function.Consumer;

public class CommandLineRunner {
    // TODO: config file
    public static String java8path = "/usr/lib/jvm/java-8-openjdk-amd64/bin/java";

    public static void runCommand(CommandLine commandLine) {
        String workingDirectory = System.getProperty("user.dir");
        System.out.println(commandLine);

        ExecuteWatchdog watchdog = new ExecuteWatchdog(96000);
        DefaultExecutor executor = new DefaultExecutor();
        executor.setWorkingDirectory(new File(workingDirectory));
        executor.setWatchdog(watchdog);

        executor.setExitValue(0);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        executor.setStreamHandler(new PumpStreamHandler(out));

        try {
            executor.execute(commandLine);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                out.flush();
                String output = out.toString();
                out.reset();
                System.out.println(output);
                out.close();
            } catch (IOException e) {
            }
        }


    }

    public static void lineIterator(File f, Consumer<String> eachLine) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(f));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            eachLine.accept(line);
        }
        bufferedReader.close();
    }
}
