# TEAW API
TAPI is a super simple plugin that replaces Dynmap's ability to see online players, as Bluemap does not provide this
when player positions are disabled. It also provides data from Towny and Vault for even more information.

The Towny and Bukkit APIs are very simple, so this could easily be expanded in the future if Theeno allows for it.

## Programmer Notes
Spark is deprecated, transition to something else. <br>
Try to actually be compliant with the [json:api spec](https://jsonapi.org/). <br>
Store player statistics locally so that offline players can still be queried.

## Configuration
TAPI implements a nano sized HTTP server for replying to requests. The only config option is the port at which
the server lives. The default is `1850`.

## Endpoints
  Most endpoint feilds are self explanitory. Where they are not, there will be a note.


- `/api/online_players` GET

  Returns a list of online players.

  Example response:
  ```json
  {
    "online_players": {
      "75418e9c-34ef-4926-af64-96d98d10954c": {
        "balance": 8234.15,
        "town": "TTown",
        "nation": "MyNation",
        "afk": true,
        "name": "brandonusa",
        "title": "geccer"
      }
    }
  }
  ```
  
- `/api/towny` GET

  Returns a list of towns and nations from the Towny plugin. It includes: `towns`, `nations`.

  `is_active` is whether the town is active or not. A town is considered active if 30% or more of its residents
  have logged on in the last 14 days.
- 
  Example response:
  ```json
  {
    "towns": {
      "8b3863d9-f83f-4e3b-a564-02fc06bdeda8": {
        "board": "money",
        "is_active": true,
        "claimed_chunks": 5,
        "mayor": "brandonusa",
        "balance": 8907,
        "name": "TTown",
        "founder": "brandonusa",
        "residents": [
          "brandonusa"
        ],
        "nation": "GexNation",
        "founding_date": 1725790166670,
        "resident_tax_percent": 23
      }
    },
    "nations": {
      "8dd4d2e5-151c-43f5-9335-4db32ae0ec8b": {
        "leader": "brandonusa",
        "capitol_town": "TTown",
        "board": "/nation set board [msg]",
        "balance": 2000,
        "town_tax_dollars": 23,
        "founding_date": 1727145046624,
        "name": "MyNation"
      }
    }
  }
  ```

- `/api/full_player_stats/:uuid` GET

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
  
- `/api/chat_history` GET

  Returns a list of the last 200 chat messages. An optional `time` argument can be provided, where only messages after
  the timestamp are provided. The `time` argument is a Unix epoch in milliseconds.
  Ex: `/api/chat_history?time=1700000000`
  The different message types are `chat`, `discord`, `join`, `quit`, `death`, & `advancement`.
  ```json
  [
    {
      "sender": "SERVER",
      "message": "brandonusa joined the game",
      "timestamp": 1732104771419,
      "type": "join"
    },
    {
      "sender": "SERVER",
      "message": "brandonusa has completed the advancement [A Pair of Giants]",
      "timestamp": 1732104772574,
      "type": "advancement"
    },
    {
      "sender": "brandonusa",
      "message": "no way, a large pair",
      "timestamp": 1732104786820,
      "type": "chat"
    },
    {
      "sender": "besser",
      "message": "chatting rn",
      "timestamp": 1732104787311,
      "type": "discord"
    },
    {
      "sender": "SERVER",
      "message": "brandonusa left the game",
      "timestamp": 1732104792573,
      "type": "quit"
    }
  ]
  ```

- `/api/server_info` GET
  
  Returns some info about the server and world.
  ```json
  {
    "tapi_version": "TAPI v1.3.0, Build 2024-11-20T12:10:35Z",
    "world_time_24h": "18:18",
    "weather": "Clear",
    "world_time_ticks": 12309,
    "server_version": "arclight-1.20.1-1.0.5-1a8925b (MC: 1.20.1)"
  }
  
  ```

## Building
The plugin is built with Maven, and is edited with IntelliJ IDEA. It is free for students.
[This video](https://www.youtube.com/watch?v=s1xg9eJeP3E) is helpful for getting started.

And that's it! It's a pretty simple plugin. Theenor please add it, Italy and I need it for our respective websites.
