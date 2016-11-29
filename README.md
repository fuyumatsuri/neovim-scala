# neovim-scala
Scala bindings for the [Neovim](https://github.com/neovim/neovim) API.

## SBT
Add dependency:

    libraryDependencies += "xyz.aoei" %% "neovim-scala" % "1.2"

## Usage
This package provides a single class which takes an `InputStream` and an `OutputStream`,
which provides bindings for the [Neovim](https://github.com/neovim/neovim) API.

Example:

```scala
import xyz.aoei.neovim._
import scala.concurrent.ExecutionContext.Implicits.global

val inputStream = ???  // get input and output streams from either starting a new
val outputStream = ??? // process or connecting to an existing one

val nvim = new Neovim(inputStream, outputStream)

nvim.onRequest((method, args, resp) => {
  // handle request
})

nvim.onNotification((method, args) => {
  // handle notification
})

nvim.command("vsp")
for {
  windows <- nvim.listWins
  window <- {
    println(windows.length) // 2
    println(windows.head.isInstanceOf[Window]) // true
    println(windows(1).isInstanceOf[Window]) // true

    nvim.setCurrentWin(windows(1))
    nvim.getCurrentWin
  }
} yield nvim.quit()
```

## Bindings
The included bindings are auto generated from `vim_get_api_info`.
If you wish to generate new bindings, you can download this repository and run:

    sbt generate
    

## Contributing
Contributations are welcome!