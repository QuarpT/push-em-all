# Push-em-all

Push-em-all is a simple stateless, distributed publish / subscribe websocket service written in Scala
using Akka Cluster. Inspired by the hosted service Pusher https://pusher.com

Define cluster seeds nodes in [application.conf](./push-em-all/src/main/resources/application.conf)

### WS Protocol

##### Send

###### Publish

```json
{ "type": "publish", "channel": "channel_name", "event": "event_name", "data": {} }
```

###### Subscribe

```json
{ "type": "subscribe", "channel": "channel_name" }
```

##### Receive

###### Message

```json
{ "channel": "channel_name", "event": "event_name", "data": {} }
```
