package xyz.aoei.neovim

import java.io.{InputStream, OutputStream}

import scala.concurrent.Future

import xyz.aoei.msgpack.rpc.{Session, ExtendedType}

class Neovim(val in: InputStream, val out: OutputStream) extends NeovimBase(in, out) {
  override def types =
    List(ExtendedType(classOf[Buffer], 0, (buf: Buffer) => buf.data,
            (bytes: Array[Byte]) => new Buffer(session, bytes)),
        ExtendedType(classOf[Window], 1, (win: Window) => win.data,
            (bytes: Array[Byte]) => new Window(session, bytes)),
        ExtendedType(classOf[Tabpage], 2, (tab: Tabpage) => tab.data,
            (bytes: Array[Byte]) => new Tabpage(session, bytes)))
  def uiAttach(width: Int, height: Int, options: Map[String, Any]): Unit =
    session.notify("nvim_ui_attach", width, height, options)
  def uiDetach(): Unit = session.notify("nvim_ui_detach")
  def uiTryResize(width: Int, height: Int): Unit = session.notify("nvim_ui_try_resize", width, height)
  def uiSetOption(name: String, value: Object): Unit = session.notify("nvim_ui_set_option", name, value)
  def command(command: String): Unit = session.notify("nvim_command", command)
  def feedkeys(keys: String, mode: String, escape_csi: Boolean): Unit =
    session.notify("nvim_feedkeys", keys, mode, escape_csi)
  def input(keys: String): Future[Int] = session.request[Int]("nvim_input", keys)
  def replaceTermcodes(str: String, from_part: Boolean, do_lt: Boolean, special: Boolean): Future[String] =
    session.request[String]("nvim_replace_termcodes", str, from_part, do_lt, special)
  def commandOutput(str: String): Future[String] = session.request[String]("nvim_command_output", str)
  def eval(expr: String): Future[Object] = session.request[Object]("nvim_eval", expr)
  def callFunction(fname: String, args: Array[Any]): Future[Object] =
    session.request[Object]("nvim_call_function", fname, args)
  def strwidth(str: String): Future[Int] = session.request[Int]("nvim_strwidth", str)
  def listRuntimePaths: Future[List[String]] = session.request[List[String]]("nvim_list_runtime_paths")
  def setCurrentDir(dir: String): Unit = session.notify("nvim_set_current_dir", dir)
  def getCurrentLine: Future[String] = session.request[String]("nvim_get_current_line")
  def setCurrentLine(line: String): Unit = session.notify("nvim_set_current_line", line)
  def delCurrentLine(): Unit = session.notify("nvim_del_current_line")
  def getVar(name: String): Future[Object] = session.request[Object]("nvim_get_var", name)
  def setVar(name: String, value: Object): Unit = session.notify("nvim_set_var", name, value)
  def delVar(name: String): Unit = session.notify("nvim_del_var", name)
  def getVvar(name: String): Future[Object] = session.request[Object]("nvim_get_vvar", name)
  def getOption(name: String): Future[Object] = session.request[Object]("nvim_get_option", name)
  def setOption(name: String, value: Object): Unit = session.notify("nvim_set_option", name, value)
  def outWrite(str: String): Unit = session.notify("nvim_out_write", str)
  def errWrite(str: String): Unit = session.notify("nvim_err_write", str)
  def errWriteln(str: String): Unit = session.notify("nvim_err_writeln", str)
  def listBufs: Future[List[Buffer]] = session.request[List[Buffer]]("nvim_list_bufs")
  def getCurrentBuf: Future[Buffer] = session.request[Buffer]("nvim_get_current_buf")
  def setCurrentBuf(buffer: Buffer): Unit = session.notify("nvim_set_current_buf", buffer)
  def listWins: Future[List[Window]] = session.request[List[Window]]("nvim_list_wins")
  def getCurrentWin: Future[Window] = session.request[Window]("nvim_get_current_win")
  def setCurrentWin(window: Window): Unit = session.notify("nvim_set_current_win", window)
  def listTabpages: Future[List[Tabpage]] = session.request[List[Tabpage]]("nvim_list_tabpages")
  def getCurrentTabpage: Future[Tabpage] = session.request[Tabpage]("nvim_get_current_tabpage")
  def setCurrentTabpage(tabpage: Tabpage): Unit = session.notify("nvim_set_current_tabpage", tabpage)
  def subscribe(event: String): Unit = session.notify("nvim_subscribe", event)
  def unsubscribe(event: String): Unit = session.notify("nvim_unsubscribe", event)
  def getColorByName(name: String): Future[Int] = session.request[Int]("nvim_get_color_by_name", name)
  def getColorMap: Future[Map[String, Any]] = session.request[Map[String, Any]]("nvim_get_color_map")
  def getApiInfo: Future[Array[Any]] = session.request[Array[Any]]("nvim_get_api_info")
  def callAtomic(calls: Array[Any]): Future[Array[Any]] = session.request[Array[Any]]("nvim_call_atomic", calls)
}

class Buffer(val session: Session, val data: Array[Byte]) extends TypeBase {
  def lineCount: Future[Int] = session.request[Int]("nvim_buf_line_count", this)
  def getLines(start: Int, end: Int, strict_indexing: Boolean): Future[List[String]] =
    session.request[List[String]]("nvim_buf_get_lines", this, start, end, strict_indexing)
  def setLines(start: Int, end: Int, strict_indexing: Boolean, replacement: List[String]): Unit =
    session.notify("nvim_buf_set_lines", this, start, end, strict_indexing, replacement)
  def getVar(name: String): Future[Object] = session.request[Object]("nvim_buf_get_var", this, name)
  def setVar(name: String, value: Object): Unit = session.notify("nvim_buf_set_var", this, name, value)
  def delVar(name: String): Unit = session.notify("nvim_buf_del_var", this, name)
  def getOption(name: String): Future[Object] = session.request[Object]("nvim_buf_get_option", this, name)
  def setOption(name: String, value: Object): Unit = session.notify("nvim_buf_set_option", this, name, value)
  def getNumber: Future[Int] = session.request[Int]("nvim_buf_get_number", this)
  def getName: Future[String] = session.request[String]("nvim_buf_get_name", this)
  def setName(name: String): Unit = session.notify("nvim_buf_set_name", this, name)
  def isValid: Future[Boolean] = session.request[Boolean]("nvim_buf_is_valid", this)
  def getMark(name: String): Future[List[Int]] = session.request[List[Int]]("nvim_buf_get_mark", this, name)
  def addHighlight(src_id: Int, hl_group: String, line: Int, col_start: Int, col_end: Int): Future[Int] =
    session.request[Int]("nvim_buf_add_highlight", this, src_id, hl_group, line, col_start, col_end)
  def clearHighlight(src_id: Int, line_start: Int, line_end: Int): Unit =
    session.notify("nvim_buf_clear_highlight", this, src_id, line_start, line_end)
}

class Window(val session: Session, val data: Array[Byte]) extends TypeBase {
  def getBuf: Future[Buffer] = session.request[Buffer]("nvim_win_get_buf", this)
  def getCursor: Future[List[Int]] = session.request[List[Int]]("nvim_win_get_cursor", this)
  def setCursor(pos: List[Int]): Unit = session.notify("nvim_win_set_cursor", this, pos)
  def getHeight: Future[Int] = session.request[Int]("nvim_win_get_height", this)
  def setHeight(height: Int): Unit = session.notify("nvim_win_set_height", this, height)
  def getWidth: Future[Int] = session.request[Int]("nvim_win_get_width", this)
  def setWidth(width: Int): Unit = session.notify("nvim_win_set_width", this, width)
  def getVar(name: String): Future[Object] = session.request[Object]("nvim_win_get_var", this, name)
  def setVar(name: String, value: Object): Unit = session.notify("nvim_win_set_var", this, name, value)
  def delVar(name: String): Unit = session.notify("nvim_win_del_var", this, name)
  def getOption(name: String): Future[Object] = session.request[Object]("nvim_win_get_option", this, name)
  def setOption(name: String, value: Object): Unit = session.notify("nvim_win_set_option", this, name, value)
  def getPosition: Future[List[Int]] = session.request[List[Int]]("nvim_win_get_position", this)
  def getTabpage: Future[Tabpage] = session.request[Tabpage]("nvim_win_get_tabpage", this)
  def getNumber: Future[Int] = session.request[Int]("nvim_win_get_number", this)
  def isValid: Future[Boolean] = session.request[Boolean]("nvim_win_is_valid", this)
}

class Tabpage(val session: Session, val data: Array[Byte]) extends TypeBase {
  def listWins: Future[List[Window]] = session.request[List[Window]]("nvim_tabpage_list_wins", this)
  def getVar(name: String): Future[Object] = session.request[Object]("nvim_tabpage_get_var", this, name)
  def setVar(name: String, value: Object): Unit = session.notify("nvim_tabpage_set_var", this, name, value)
  def delVar(name: String): Unit = session.notify("nvim_tabpage_del_var", this, name)
  def getWin: Future[Window] = session.request[Window]("nvim_tabpage_get_win", this)
  def getNumber: Future[Int] = session.request[Int]("nvim_tabpage_get_number", this)
  def isValid: Future[Boolean] = session.request[Boolean]("nvim_tabpage_is_valid", this)
}
