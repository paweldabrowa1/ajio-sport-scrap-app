import cmd.Command;
import cmd.map.MapCommand;
import db.Database;
import db.map.MapDatabaseTable;
import org.apache.commons.cli.*;
import org.example.cmd.list.ListCommand;
import org.example.cmd.scrap.ScrapCommand;

import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args)  {
        Database db = new Database();
        db.connect();

        MapDatabaseTable mapDatabaseTable = new MapDatabaseTable(db);
        mapDatabaseTable.create();

        String programName = args[0];

        ArrayList<Command> commandList = new ArrayList<>();
        commandList.add(new MapCommand(mapDatabaseTable));
        commandList.add(new ListCommand(mapDatabaseTable));
        commandList.add(new ScrapCommand(mapDatabaseTable));

        String commandsArgName = commandList.stream().map(Command::getName).collect(Collectors.joining(" | "));

        if (args.length < 2) {
            System.out.printf("Usage: %s <%s>\n", args[0], commandsArgName);

            System.exit(1);
        }

        String cmdName = args[1];

        Optional<Command> executeCommand = commandList.stream()
                .filter(command -> command.getName().equals(cmdName))
                .findAny();

        if (executeCommand.isEmpty()) {
            System.out.printf("Command with name: '%s' not found!\n", cmdName);
            System.out.printf("Usage: %s <%s>\n", args[0], commandsArgName);

            System.exit(1);
        }

        Options options = new Options();

        Option input = new Option(
                "h", "help",
                false, "shows help form command"
        );
        options.addOption(input);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        executeCommand.get().onSetup(options);

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp(programName, options);

            System.exit(1);
        }

        if (cmd.hasOption("help")) {
            formatter.printHelp(programName, options);

            System.exit(0);
        }

        executeCommand.get().onExecute(cmd);
    }
}
