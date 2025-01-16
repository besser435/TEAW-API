# TEAW API
TAPI is a super simple plugin that replaces Dynmap's ability to see online players, as Bluemap does not provide this
when player positions are disabled. It also provides data from Towny and Vault for even more information.

The Towny and Bukkit APIs are very simple, so this could easily be expanded in the future if Theeno allows for it.

## Programmer Notes & To Do
Spark isn't updated anymore, transition to something else like Javalin. <br>
Try to actually be compliant with the [json:api spec](https://jsonapi.org/). <br>
Store player statistics locally so that offline players can still be queried.

Data is **not** normalized. For example, we have Towny UUIDs and names in several endpoints. 
In the `online_players` endpoint, we have `town`, `town_name`, `nation`,`nation_name`. And in the towny endpoint, 
we have a `residents` array with player UUIDs. This is nice as we don't have to query `online_players`, then query `towny`
to get the name of a Town a player is in. But it is bad, as we repeat a lot of information. 

When it comes to building the database (external project that relies on TAPI), it will be larger than it has to as the data is not normalized. This is bad,
and should be addressed in the future.

## Configuration
In the `config.yml` file, there are a few options. `enable`, `port`, `discord_channel_id`, & `afk_timeout`.

TAPI implements a nano sized HTTP server for replying to requests, and as such
needs to live on a port. The default port for the server is 1850.


## Endpoints
  Most endpoint fields are self-explanatory. Where they are not, there will be a note.


### `/api/online_players` GET

Returns a list of online players.

`afk_duration` Is the AFK duration for a player in milliseconds if they are AFK.
If the player has moved within the configured AFK threshold, this will be 0.


Example response:
```json
{
  "online_players": {
    "75418e9c-34ef-4926-af64-96d98d10954c": {
      "name": "brandonusa",
      "online_duration": 5424,
      "afk_duration": 0,
      "balance": 27260.0,
      "title": "geccar",
      "town": "1bfd162d-0b88-493f-a9d4-aa00f3401a37",
      "town_name": "TTown",
      "nation": "213a493f-02f4-499d-999f-4d371f839bb3",
      "nation_name": "MyNation"
    }
  }
}
```

### `/api/towny` GET

Returns a list of towns and nations from the Towny plugin. It includes: `towns`, `nations`.

`is_active` is whether the town is active or not. A town is considered active if 30% or more of its residents
have logged on in the last 14 days.

Example response:
```json
{
  "towns": {
    "1bfd162d-0b88-493f-a9d4-aa00f3401a37": {
      "resident_tax_percent": 0.0,
      "is_active": true,
      "nation": "213a493f-02f4-499d-999f-4d371f839bb3",
      "mayor": "brandonusa",
      "founder": "brandonusa",
      "founded": 1732767406676,
      "color_hex": "000000",
      "nation_name": "MyNation",
      "spawn_loc_z": 96,
      "spawn_loc_y": 77,
      "spawn_loc_x": 18,
      "balance": 119002.0,
      "name": "TTown",
      "residents": [
        "75418e9c-34ef-4926-af64-96d98d10954c"
      ],
      "claimed_chunks": 3,
      "tag": "TT",
      "board": "/town set board [msg]"
    }
  },
  "nations": {
    "213a493f-02f4-499d-999f-4d371f839bb3": {
      "leader": "brandonusa",
      "capitol_town": "1bfd162d-0b88-493f-a9d4-aa00f3401a37",
      "balance": 60000.0,
      "town_tax_dollars": 0.0,
      "name": "MyNation",
      "founded": 1733267199029,
      "capitol_town_name": "TTown",
      "color_hex": "00ffff",
      "tag": "MN",
      "board": "/nation set board [msg]"
    }
  }
}
```

### `/api/full_player_stats/:uuid` GET

Returns the three [statistics](https://minecraft.wiki/w/Statistics) categories for a given player UUID. Stats with 
a zero value will not be returned. The player must be online for success, otherwise it will return 404.

Example response:
```json
{
  "general": {
    "DAMAGE_TAKEN": 230,
    "LEAVE_GAME": 243,
    "FALL_ONE_CM": 6911
  },
  "mob": {
    "KILL_ENTITY": {
      "FROG": 9,
      "SALMON": 1,
      "TRADER_LLAMA": 2
    }
  },
  "item": {
    "PICKUP": {
      "DIAMOND_BLOCK": 1,
      "DIAMOND": 3420,
      "FISHING_ROD": 2
    },
    "USE_ITEM": {
      "NETHERITE_BLOCK": 2,
      "MANGROVE_LEAVES": 6,
      "CREATE_MECHANICAL_BEARING": 9
    },
    "DROP": {
      "CREATE_CREATIVE_FLUID_TANK": 1,
      "DIAMOND": 340,
      "FISHING_ROD": 1
    }
  }
}
```
  
### `/api/chat_history` GET

Returns a list of the last 100 chat messages. An optional `time` argument can be provided, where only messages after
the timestamp are provided. The `time` argument is a Unix epoch in milliseconds.
Ex: `/api/chat_history?time=1700000000`
The different message types are `chat`, `discord`, `join`, `quit`, `death`, `status` & `advancement`.

The `sender_uuid` field is not always the sender's UUID. For messages types like a death or advancement which are SERVER
messages, we instead include the UUID of the player the message is about. For Discord messages, we send the user ID of the 
message's author. Messages that aren't related to a player (such as a status message) have their UUID empty.

Example response:
```json
[
  {
    "sender": "SERVER",
    "sender_uuid": "",
    "message": "TEAW started!",
    "timestamp": 1732104614300,
    "type": "status"
  },
  {
    "sender": "SERVER",
    "sender_uuid": "75418e9c-34ef-4926-af64-96d98d10954c",
    "message": "brandonusa joined the game",
    "timestamp": 1732104771419,
    "type": "join"
  },
  {
    "sender": "SERVER",
    "sender_uuid": "75418e9c-34ef-4926-af64-96d98d10954c",
    "message": "brandonusa has completed the advancement [A Pair of Giants]",
    "timestamp": 1732104772574,
    "type": "advancement"
  },
  {
    "sender": "brandonusa",
    "sender_uuid": "75418e9c-34ef-4926-af64-96d98d10954c",
    "message": "no way, a large pair",
    "timestamp": 1732104786820,
    "type": "chat"
  },
  {
    "sender": "besser",
    "sender_uuid": "232014294303113216",
    "message": "chatting rn",
    "timestamp": 1732104787311,
    "type": "discord"
  },
  {
    "sender": "SERVER",
    "sender_uuid": "75418e9c-34ef-4926-af64-96d98d10954c",
    "message": "brandonusa left the game",
    "timestamp": 1732104792573,
    "type": "quit"
  }
]
```

### `/api/server_info` GET

Returns some info about the server and world.

Example response:
```json
{
  "tapi_version": "1.4.3",
  "system_time": 1733635909945,
  "world_time_24h": "06:10",
  "weather": "Thunderstorms",
  "tapi_build": "2024-12-08T05:29:19Z",
  "world_time_ticks": 180,
  "server_version": "arclight-1.20.1-1.0.5-1a8925b (MC: 1.20.1)",
  "day": 756
}

```

### API Errors
Any error in the response will be returned as a JSON object along with its HTTP status.

Example: `{"error": "Not found"}` for a 404, or `{"error": "UUID malformed"}` for a 400.

## Building
The plugin is built with Maven, and is edited with IntelliJ IDEA. It is free for students.
[This video](https://www.youtube.com/watch?v=s1xg9eJeP3E) is helpful for getting started.

And that's it! It's a pretty simple plugin. Theenor please add it, Italy and I need it for our respective websites.
