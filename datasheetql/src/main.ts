import { program } from "commander"
import * as command from "./command"

program.name("DatasheetQL").description("CLI to execute DatasheetQL queries")

program
  .command("exec")
  .argument("<file>", "DatasheetQL source file")
  .requiredOption("-t, --api-token <api-token>", "Fusion API token")
  .requiredOption("-s, --space <space>", "space name")
  .option(
    "-h, --host <host>",
    "Fusion API host, can be one of 'production' and 'integration', or an arbitrary URL",
  )
  .action((filePath, options) => {
    let { space: spaceName, apiToken, host } = options
    if (host === undefined || "production".startsWith(host)) {
      host = "https://api.vika.cn/fusion/v1"
    } else if ("integration".startsWith(host)) {
      host = "https://integration.vika.ltd/fusion/v1"
    } else {
      try {
        new URL("", host)
      } catch (e) {
        if (e instanceof TypeError) {
          console.error(`${host} is not a valid URL`)
          process.exit(1)
        } else {
          throw e
        }
      }
    }
    command.exec.runCommand({
      filePath,
      spaceName,
      apiToken,
      host,
    })
  })

program
  .command("explain")
  .argument("<file>", "DatasheetQL source file")
  .action((filePath) => {
    command.explain.runCommand({
      filePath,
    })
  })

program.parse()