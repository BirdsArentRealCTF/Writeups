import time
import frida
import json

jscode = """
Java.perform(function x() {
  var nx = Java.use("nx");
  nx.a.overload();
  var nxab = nx.a.overload("java.lang.String");
  nx.$init.overload(
    "java.lang.String",
    "android.content.res.Resources"
  ).implementation = function (x, y) {
    console.log("nx init", x, y);
    return this.$init(x, y);
  };
  nxab.implementation = function (x) {
    console.log("nx.a", x);
    return this.a(x);
  };
  nx.b.overload("java.lang.String").implementation = function (x) {
    console.log("nx.b", x);
    return this.b(x);
  };
  nx.c.overload("java.lang.String").implementation = function (x) {
    console.log("nx.c", x);
    return this.c(x);
  };
  var SecretKey = Java.use("javax.crypto.spec.SecretKeySpec");
  SecretKey.$init.overload("[B", "java.lang.String").implementation = function (
    x,
    y
  ) {
    send("KEY", new Uint8Array(x));
    return this.$init(x, y);
  };
  var IV = Java.use("javax.crypto.spec.IvParameterSpec");
  IV.$init.overload("[B").implementation = function (x) {
    send("IV", new Uint8Array(x));
    return this.$init(x);
  };
});
"""


def msg(m, p):
    if m["type"] == "send":
        print(m["payload"], p.hex())
    else:
        print(m)
        print('*' * 16)
        print(p)


device = frida.get_usb_device()
pid = device.spawn(["wtf.riceteacatpanda.quiz"])
device.resume(pid)
time.sleep(1)
session = device.attach(pid)
script = session.create_script(jscode)
script.on("message", msg)
script.load()
input()
