# v2 dev notes and todo:
* The JDBC SQLite driver dependency is a bit janky. Might conflict with other mods depending on how they load their
SQLite driver. 


* TAPI v1 should remain as an old branch, where v2 is the new default branch. Do the same for the TEAW website.


* Normalize data. This will make querying the database harder, but will drastically cut down on the DB size.



# TEAW API 2
TAPI 2 is a super simple server-side mod that updates an SQLite database with information about the ToEndAllWars Minecraft server. 
It shows things like online players, chat history, player statistics, and more.





## Configuration
In the `config.yml` file, there are a few options. `enable`, `discord_channel_id`, & `afk_timeout`.


