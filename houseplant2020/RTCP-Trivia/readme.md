​	Hello, this is enjloezz and I played this ctf with Albytross team. It was great to finish all challenges nearly 16 hours before ctf ended. Thanks to challenge authors it was really fun ctf . I’m not a reverse engineer or any kind of reverser. Please mind that :)

​	We know its a trivia game and we need to solve 1000 questions to get the flag. But we can’t play it, I don’t know why but intent is closing without waiting the CountDownTimer. I tried couple methods to bypass that but I was unable to do it. After ctf is finished one of my  teammates told me its because of using custom emulator. Game working as intended on android studio emulator with the sdk version from manifest.xml! I wish I knew sooner :(

Then I started to listen the socket protocol, when we enter user name its sending a request.

```json
{"method":"ident","userToken":"9ba05f588e86238984b4965eaa56f49bd21c2f69c317882e5fd84aff9a5e4385"}
```

First idea of userToken was sha256(username) but thats not the case. If we look closely to algorithm its purely random :)

```java
final StringBuffer sb = new StringBuffer();
while (sb.length() < 64) {
	sb.append(Integer.toHexString(random.nextInt()));
}
final String substring = sb.toString().substring(0, 64);
```

Before forgetting it, everytime you send a request its sending a success response like;

```json
{"method":"methodName","success":true}
```

So my idea was write a custom socket client and try to reproduce this game. First step is figure out how questions delivered? 

Answer is send request to socket and get the response.

```json
{"method":"start"}
```

After this request server sends one question everytime. 

```json
{"method":"question","id":"0acfdfca-3543-48a6-a290-b95a449b8e66","questionText":"m2K5LEjFUCgXFPROL4WlzZbgqoHGCuHgoBo8tG92WqP0K6XrgQIdS4dQ/yB+yleMKoFgVDR6gLcT8qcZE9Kz4cq0tuEqtCtFtrKWVRYZeE0=","options":["X2z/7l6+mygiwrE6AOdkJw==","OnOVHe/7UtoSFlfJkT1H7Q==","Z3+Fgtrct2O7u1hqsAKcKw==","576l1csP0AX5Y6EcaKo2Qg=="],"correctAnswer":"EShNMH7MU91HQtIrbJCkhxHZKZEmwpmKqALVuoAdx24=","requestIdentifier":"bc1e6c71224e40d76c19b1d76d3db662"}
```

![image-20200427005135677](https://github.com/BirdsArentRealCTF/RTCP-Trivia/raw/master/image-20200427005135677.png)

Every field that matters is aes-cbc encrypted with pkcs7 padding. So my idea was using frida get the KEY and IV for script you can look at my github repo (https://github.com/BirdsArentRealCTF/RTCP-Trivia).

```
KEY 61798024a3e9bb3b4be28cca863af54b2af0b4b27248599c8b7a3b1c179296bc
IV bc1e6c71224e40d76c19b1d76d3db662
```

Looks like IV is requestIdentifier but for key we need to make some calculations :(

I'm gonna explain the image below line by line bear with me :)

```java
final JSONObject jsonObject = new JSONObject(this.d); // => Creating JSON-Object from the response.
final String a = new nx(Game.this.getIntent().getStringExtra("id"), Game.this.getResources()).a(); // => Generating a new nx object that taking two parameters. First one is userToken and second one is content.res.Resources object. I will explain why we are sending Resources object. And then call a function in nx class.
final String string = jsonObject.getString("id"); // => Getting ID field from the response.
final StringBuilder sb = new StringBuilder();
sb.append(a);
sb.append(":");
sb.append(string); // => Combine return value of nx.a() method with id field from the response.
final byte[] a2 = nx.a(sb.toString()); // => Send sb variable to nx.a() method and get the return value.
final byte[] b = nx.b(jsonObject.getString("requestIdentifier")); // => Getting requestIdentifier field for using as IV
final SecretKeySpec secretKeySpec = new SecretKeySpec(a2, "AES"); // => Create an AES KEY Object with a2 variable key.
final IvParameterSpec IvParameterSpec = new IvParameterSpec(b); // => Create an IV Parameter Object.
final Cipher Instance = Cipher.getInstance("AES/CBC/PKCS7Padding"); // => Create a Cipher Instance with given specs. "AES-CBC with PKCS7 Padding".
Instance.init(2, secretKeySpec, IvParameterSpec); // => And initialize AES decoder.
```

Now we know how its decoding the response. Only thing we don't know is how nx class works. Like I said I'm not reverse engineer so for understanding this part correctly you need to check other writeups because I will skip whole resource part with dynamic analysis.

```java
private static final char[] c;
private Resources a;
private String b;

static {
    c = "0123456789ABCDEF".toCharArray();
}

public nx(final String b, final Resources a) {
    this.b = b; // userToken
    this.a = a; // Resource
}
```
**There are two a method, if you don't know what method overloading is you should look at it before reading this part.**

```java
public static byte[] a(final String s) {
	return MessageDigest.getInstance("SHA-256").digest(s.getBytes());
} // a("string") = sha256("string")

public final String a() {
    final InputStream openRawResource = this.a.openRawResource(2131427328);
    final byte[] array = new byte[openRawResource.available()];
    final byte[] array2 = new byte[openRawResource.available()];
    openRawResource.read(array);
    openRawResource.close();
    new ArrayList();
    final int n = 0;
    for (int i = 0; i < array.length; ++i) {
        final double n2 = i;
        if (Math.sqrt(n2) % 1.0 == 0.0) {
            array2[(int)Math.sqrt(n2)] = array[i];
        }
    }
    final byte[] digest = MessageDigest.getInstance("SHA-256").digest(array2);
    final StringBuffer sb = new StringBuffer();
    for (int j = n; j < digest.length; ++j) {
        final String hexString = Integer.toHexString(0xFF & digest[j]);
        if (hexString.length() == 1) {
            sb.append('0');
        }
        sb.append(hexString);
    }
    return this.c(String.valueOf(sb));
}
```

This where I skip because I didn't extract the resource 2131427328. Instead of extracting it I focused return part. Its calling c method with sb parameter. So I wrote a frida hook to get the parameter. 

```
nx.c cbce23dfcdc7efe826d23bbf3d635d8fd55b6499d16ca8830a973ff57175119f
```

This method is always getting same parameter. With this information I don't have to re-write nx.a() method or extract any resource.

```java
private String c(final String s) {
    final MessageDigest Instance = MessageDigest.getInstance("SHA-256");
    // Important Part
    final StringBuilder sb = new StringBuilder();
    sb.append(s);
    sb.append(":");
    sb.append(this.b);
    final byte[] digest = Instance.digest(new String(sb.toString()).getBytes());
    // After this line is just for padding
    final StringBuffer sb2 = new StringBuffer();
    for (int i = 0; i < digest.length; ++i) {
        final String hexString = Integer.toHexString(0xFF & digest[i]);
        if (hexString.length() == 1) {
            sb2.append('0');
        }
        sb2.append(hexString);
    }
    return String.valueOf(sb2);
}
```
As we can see its basically doing this; 

```
sha256("cbce23dfcdc7efe826d23bbf3d635d8fd55b6499d16ca8830a973ff57175119f:this.b")
```

And we know this.b is userToken, so its becoming;

```
sha256("cbce23dfcdc7efe826d23bbf3d635d8fd55b6499d16ca8830a973ff57175119f:userToken")
```

If we go back to generating AES key part

```java
final String a = new nx(Game.this.getIntent().getStringExtra("id"), Game.this.getResources()).a();
final String string = jsonObject.getString("id");
final StringBuilder sb = new StringBuilder();
sb.append(a);
sb.append(":");
sb.append(string);
final byte[] a2 = nx.a(sb.toString());
```

AES key is;

```javascript
var f = sha256("cbce23dfcdc7efe826d23bbf3d635d8fd55b6499d16ca8830a973ff57175119f:userToken");
var key = sha256(f + ":" + "requestId");
```

Finally we can generate Key and IV without using apk. After here is pretty straight forward. Flow of client is;

```javascript
var userToken = "64 char random value";
var f = sha256("cbce23dfcdc7efe826d23bbf3d635d8fd55b6499d16ca8830a973ff57175119f:userToken");
Send	=> {"method":"ident", "userToken": "userToken"}
Receive => {"method":"ident","success":true}
Send	=> {"method":"start"}
Receive => {"method":"start","success":true}
//---------------------------LOOP 1000 Times---------------------------
Receive => {"method":"question","id":"dd85fdf1-8b7f-4b91-a43e-0a15235164fd","questionText":"IvP7c0zRIYoJlT+pJIkB1+oqpzUYf6I2M1eFN7euCk7rO/zTqDtdm3Axn0D+Pc2DPVzyRngudjFRk02yHi+7qBjcHqLoPkNDJI96gBTxSLo=","options":["uYHfqGdCnH10uE58i4WKSksfICXLugaR/qTCLx536YU=","1CgutfAAiFrRgqV8ho/DBA==","T8aYSUNeyOX1XmMdireoeybNS9CwoGZwVzCa0ahVic8=","qym/uzpEKRnmNgEpY4dj8s77/SzC8i/2hHChstSTI0Q="],"correctAnswer":"fF7GPXx/HSds3KPUEXAXjhewX+ICi+z16tnr5VJU+cs=","requestIdentifier":"b14f8dfaee7511fe040edef6ec908541"}
var response = JSON.parse(response);
var key = sha256(f + ":" + response.id);
var iv = response.requestIdentifier;
var correctAnswer = AES(key, iv).decrypt(response.correctAnswer).clearPadding();
Send 	=> {"method": "answer","answer": correctAnswer}
//---------------------------LOOP 1000 Times---------------------------
Receive => {"method": "flag","flag":"rtcp{qu1z_4pps_4re_c00l_aeecfa13}"}
```
I hope you can understand idea of this challenge. Do you have any questions or suggestion? Feel free to contact via discord. **enjloezz#7444**.

References:

https://github.com/frida/frida/issues/272#issuecomment-299877883

https://11x256.github.io/Frida-hooking-android-part-5/
