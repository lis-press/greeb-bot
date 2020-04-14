# How to work with WorkFlowy internal API?

WorkFlowy doesn't have official API but we can use internal methods, used on FrontEnd.

In this doc I will show how to use it, will use `curl` in order to demonstrate examples.

## Authorization

First try to check the type of authorization:
```bash
curl -X POST -F 'email={YOUR_EMAIL}' https://workflowy.com/api/auth
```

Substitute `{YOUR_EMAIL}`, `{YOUR_PASSOWRD}` and `{YOUR_CODE}` with your real email, 
password and code in this and next examples.


### Password

In case you have password authorization: `{"authType": "password"}`.

In that case you need to send your password:
```bash
curl -X POST -F 'email={YOUR_EMAIL}' -F 'password={YOUR_PASSWORD}' https://workflowy.com/api/auth -D -
```

Result:
```bash
HTTP/2 200 
date: Tue, 14 Apr 2020 19:51:49 GMT
content-type: application/json
content-length: 37
server: nginx/1.17.9
vary: Cookie,Origin,Accept-Encoding
strict-transport-security: max-age=31536000; includeSubDomains; preload
x-xss-protection: 1; mode=block
set-cookie: sessionid={YOUR_SESSION_ID}; expires=Tue, 13 Oct 2020 19:51:49 GMT; HttpOnly; Max-Age=15724800; Path=/; Secure
cache-control: no-cache
x-content-type-options: nosniff

{"success": true, "isNewUser": false}
```

You're interested in `{YOUR_SESSION_ID}` (`set-cookie:` line), it will be used in the next part.

### Code authorization

In case of code authorization you'll have: `{"authType": "code"}`.

You'll get an email with code and you can authorize using it:
```bash
curl -X POST -F 'email={YOUR_EMAIL}' -F 'code={YOUR_CODE}' https://workflowy.com/api/auth
```

The result is the same as in previous example.


## Getting data from WorkFlowy

Now we're ready to use WorkFlowy, to get data you have just one query that returns all you tree:
```bash
curl -H "cookie: sessionid={YOUR_SESSION_ID}" https://workflowy.com/get_initialization_data\?client_version\=21 > workflowy.json
```

See [Authorization](#authorization) part about getting the data from WorkFlowy.
Be aware, that it's internal API, version can be sunsetted and may be incompatible.

### Exploring data 

As a result we have a big json. 
My IDE index it quite slowly, so I prefer to use `less`, [`gron`](https://github.com/tomnomnom/gron) and `grep`.

To get it from JSON I can use `gron`, it makes JSON easily greppable, the workflowy file will looke like this:
```bash
gron workflowy.json
```

Will reutrn
```bash
json = {};
json.features = [];
json.features[0] = {};
json.features[0].codename = "beta_redirect";
json.features[0].description = "Help test new WorkFlowy releases by using beta.workflowy.com.";
json.features[0].name = "WorkFlowy Beta";
json.features[1] = {};
json.features[1].codename = "open_links_in_desktop";
json.features[1].description = "When opening links in the browser, also open them in the desktop.";
json.features[1].name = "Open links in app";
```

#### Example of list
This form is easy-to-grep.

For example I've made a list:
```
* Test
  * First
  * Second
```

The link to this list is: https://workflowy.com/#/a811b734c17f

So I'll try to grep it:
```bash
gron workflowy.json | grep -F "a811b734c17f"
```

Return looks like:
```bash
json.projectTreeData.mainProjectTreeInfo.rootProjectChildren[0].ch[3].id = "6b5d2105-212a-c421-f1f6-a811b734c17f";
```

We can make the full JSON via the string that goes before `.id = `:
```bash
gron workflowy.json | grep -F "a811b734c17f" | awk -F'.id = ' '{print $1}'
```

Will give:
```bash
json.projectTreeData.mainProjectTreeInfo.rootProjectChildren[0].ch[3]
```

So let's grep the full JSON for this list:
```bash
gron workflowy.json | grep -F $(gron workflowy.json | grep -F "a811b734c17f" | awk -F'.id = ' '{print $1}') | gron --ungron
```

The result looks like:
```json
{
  "projectTreeData": {
    "mainProjectTreeInfo": {
      "rootProjectChildren": [
        {
          "ch": [
             null,
             null,
             null,
            {
              "ch": [
                {
                  "id": "cb4e4374-f555-c87b-435c-12d864843dd3",
                  "lm": 146062656,
                  "metadata": {},
                  "nm": "First"
                },
                {
                  "id": "e39551f2-d3a4-9bea-caf8-2e798adc21a4",
                  "lm": 146062662,
                  "metadata": {},
                  "nm": "Second"
                }
              ],
              "id": "6b5d2105-212a-c421-f1f6-a811b734c17f",
              "lm": 146062654,
              "metadata": {},
              "nm": "Test"
            }
          ]
        }
      ]
    }
  }
}
```

## Writing data to WorkFlowy 
TBA
