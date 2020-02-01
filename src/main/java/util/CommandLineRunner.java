package util;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.function.Consumer;

public class CommandLineRunner {
    public static void runCommand(CommandLine commandLine) throws IOException {
        String workingDirectory = System.getProperty("user.dir");
        System.out.println(commandLine);

        ExecuteWatchdog watchdog = new ExecuteWatchdog(96000);
        DefaultExecutor executor = new DefaultExecutor();
        executor.setWorkingDirectory(new File(workingDirectory));
        executor.setWatchdog(watchdog);

        executor.setExitValue(0);

        executor.execute(commandLine);
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
