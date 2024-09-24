# TEAW API
TAPI is a super simple plugin that replaces Dynmap's ability to see online players, as Bluemap does not provide this
when player positions are disabled. It also provides data from Towny.

The Towny and Bukkit APIs are very simple, so this could easily be expanded in the future if Theeno allows for it.

## Programmer Notes
Spark is deprecated, transition to something else. <br>
Maybe try to actually be compliant with the [json:api spec](https://jsonapi.org/).

## Configuration
TAPI implements a nano sized HTTP server for replying to requests. The only config option is the port at which
the server lives. The default is `1850`.

## Endpoints
- `/api/online_players` GET

  Returns a list of online players. It includes: `uuid`, `town`, `nation`, `afk`, `name`.

  Example response:
  ```json
  {
    "online_players": {
      "75418e9c-34ef-4926-af64-96d98d10954c": {
        "balance": 8234.15,
        "town": "TTown",
        "nation": "MyNation",
        "afk": true,
        "name": "brandonusa"
      }
    }
  }
  ```
  
- `/api/towny` GET

  Returns a list of towns and nations from the Towny plugin. It includes: `towns`, `nations`.
  
  Example response:
  ```json
  {
    "towns": {
      "8b3863d9-f83f-4e3b-a564-02fc06bdeda8": {
        "board": "money",
        "town_size": 5,
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
  
## Building
The plugin is built with Maven, and is edited with IntelliJ IDEA. It is free for students.
[This video](https://www.youtube.com/watch?v=s1xg9eJeP3E) is helpful for getting started.

And that's it! It's a pretty simple plugin. Theenor please add it, Italy and I need it for our respective websites.