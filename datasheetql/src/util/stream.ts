import tty from "tty"

export function streamToString(stream: tty.ReadStream): Promise<string> {
  const chunks: Buffer[] = []
  return new Promise((resolve, reject) => {
    stream.on("data", (chunk) => chunks.push(Buffer.from(chunk)))
    stream.on("error", (err) => reject(err))
    stream.on("end", () => resolve(Buffer.concat(chunks).toString("utf8")))
  })
}
