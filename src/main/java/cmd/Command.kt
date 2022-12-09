package cmd

import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Options
import org.example.db.map.MapTableDao

abstract class Command(val name: String, val mapTable: MapTableDao) {

    abstract fun onSetup(options: Options)
    abstract fun onExecute(cmd: CommandLine)
}