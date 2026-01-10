#
# ModelEngine 4 wrapper tasks
#

modelengine_spawn_model:
  type: task
  debug: true
  definitions: model_name|location|tracking_range|viewer
  script:
  - define location <[location].as_location>
  - define tracking_range <[tracking_range].if_null[256]>
  - define viewer_name <[viewer].if_null[server].name.if_null[server]>
  - define command "modelengine spawn <[model_name]> <[location.x]> <[location.y]> <[location.z]> <[location.world.name]> <[location.yaw]> <[location.pitch]> <[tracking_range]> <[viewer_name]>"
  - define before_entities <[location].find_entities.within[4]||<list>>
  - execute as_server <[command]> silent
  - wait 1t
  - define after_entities <[location].find_entities.within[4]||<list>>
  - define spawned_entity <[after_entities].exclude[<[before_entities]>].first||<[after_entities].first||null>
  - definemap result command:<[command]> model:<[model_name]> location:<[location]> viewer:<[viewer]> entity:<[spawned_entity]>
  - flag server dcutscene_modelengine.last_spawn:<[result]>
  - if <[viewer].is_player||false>:
    - flag <[viewer]> dcutscene_modelengine.last_spawn:<[result]>
  - determine <list[<[spawned_entity]>]>

modelengine_animate:
  type: task
  debug: true
  definitions: entity|animation
  script:
  - define command "modelengine animation play <[entity].uuid> <[animation]>"
  - execute as_server <[command]> silent
  - definemap result command:<[command]> entity:<[entity]> animation:<[animation]>
  - flag server dcutscene_modelengine.last_animation_play:<[result]>
  - if <[entity].is_spawned||false>:
    - flag <[entity]> dcutscene_modelengine.last_animation_play:<[result]>

modelengine_end_animation:
  type: task
  debug: true
  definitions: entity
  script:
  - define command "modelengine animation stop <[entity].uuid>"
  - execute as_server <[command]> silent
  - definemap result command:<[command]> entity:<[entity]>
  - flag server dcutscene_modelengine.last_animation_stop:<[result]>
  - if <[entity].is_spawned||false>:
    - flag <[entity]> dcutscene_modelengine.last_animation_stop:<[result]>

modelengine_delete:
  type: task
  debug: true
  definitions: entity
  script:
  - define command "modelengine delete <[entity].uuid>"
  - execute as_server <[command]> silent
  - definemap result command:<[command]> entity:<[entity]>
  - flag server dcutscene_modelengine.last_delete:<[result]>
  - if <[entity].is_spawned||false>:
    - flag <[entity]> dcutscene_modelengine.last_delete:<[result]>

modelengine_reset_model_position:
  type: task
  debug: false
  definitions: entity
  script:
  - define command "modelengine reset_position <[entity].uuid>"
  - execute as_server <[command]> silent
  - definemap result command:<[command]> entity:<[entity]>
  - flag server dcutscene_modelengine.last_reset_position:<[result]>
  - if <[entity].is_spawned||false>:
    - flag <[entity]> dcutscene_modelengine.last_reset_position:<[result]>
