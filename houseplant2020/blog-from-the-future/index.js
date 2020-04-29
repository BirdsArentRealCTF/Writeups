window.buffer = require("buffer/").Buffer;
window.md5 = require("md5");
window.aesjs = require("aes-js");
window.msgpack = require("msgpack-lite");
window.randomBytes = require("randombytes");
window.logMessage = (type, message) => {
  let x = document.getElementsByClassName(type);
  if (x.length === 0) {
    let el = document.createElement("span");
    el.className = type;
    el.innerText = message;
    logs.prepend(document.createElement("br"));
    logs.prepend(el);
  } else {
    x[0].innerText = message;
  }
};
