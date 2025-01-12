import chalk from "chalk"
import { CliUx } from "@oclif/core"

export const Terminal = {
  info(message: string) {
    console.log(chalk.bgGreen('INFO') + '  ' + message)
  },
  error(message: string) {
    console.log(chalk.bgRed('ERROR') + ' ' + message)
  },
  warn(message: string) {
    console.log(chalk.bgYellow('WARN') + '  ' + message)
  },
  startSpinner(message: string) {
    CliUx.ux.action.start(message, undefined, { stdout: true })
  },
  stopSpinner() {
    CliUx.ux.action.stop()
  }
}