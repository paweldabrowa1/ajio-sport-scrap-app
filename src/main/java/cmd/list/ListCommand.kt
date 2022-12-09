package org.example.cmd.list

import cmd.Command
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options
import org.example.db.map.MapTableDao

class ListCommand(mapTable: MapTableDao) : Command("list", mapTable) {

    override fun onSetup(options: Options) {
        options.addOption(Option(
            "e", "errored",
            false, "shows only errored playing fields"
        ))
    }

    override fun onExecute(cmd: CommandLine) {
        val list = if (cmd.hasOption("errored")) {
            mapTable.getAllErrored()
        } else {
            mapTable.getAll()
        }

        println("Playing fields (${list.size}):")
        for ((index, field) in list.withIndex()) {
            println("$index:\t$field")
        }
    }

}