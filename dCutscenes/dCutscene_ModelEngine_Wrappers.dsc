#
# ModelEngine 4 wrapper tasks
#

modelengine_spawn_model:
  type: task
  debug: true
  definitions: model_name|location|tracking_range|viewer
  script:
  - define location <[location].as[location]>
  - define tracking_range <[tracking_range].if_null[256]>
  - define viewer <[viewer].if_null[server]>
  - define viewer_name <[viewer].name.if_null[server]>
  - define command "modelengine spawn <[model_name]> <[location].x> <[location].y> <[location].z> <[location].world.name> <[location].yaw> <[location].pitch> <[tracking_range]> <[viewer_name]>"
  - define before_entities <[location].find_entities.within[4]||<list>>
  - execute as_server <[command]> silent
  - wait 2t
  - define after_entities <[location].find_entities.within[4]||<list>>
  - define new_entities <[after_entities].exclude[<[before_entities].parse_tag>]>
  - define spawned_entity <[new_entities].first||<[after_entities].first||null>
  - define fallback_entity <[viewer].if_null[server].flag[dcutscene_modelengine.last_spawn.entity]||<server.flag[dcutscene_modelengine.last_spawn.entity]>>
  - if <[spawned_entity].is_null||false> && <[fallback_entity].is_spawned||false>:
    - define spawned_entity <[fallback_entity]>
  - if <[spawned_entity].is_null||false>:
    - debug "modelengine_spawn_model: failed to resolve spawned entity for <[model_name]> near <[location]>. No new entity found and last_spawn flag missing or invalid."
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

modelengine_skill:
  type: task
  debug: true
  definitions: entity|skill
  script:
  - define command "modelengine skill <[entity].uuid> <[skill]>"
  - execute as_server <[command]> silent
  - definemap result command:<[command]> entity:<[entity]> skill:<[skill]>
  - flag server dcutscene_modelengine.last_skill:<[result]>
  - if <[entity].is_spawned||false>:
    - flag <[entity]> dcutscene_modelengine.last_skill:<[result]>

modelengine_state:
  type: task
  debug: true
  definitions: entity|state
  script:
  - define command "modelengine state <[entity].uuid> <[state]>"
  - execute as_server <[command]> silent
  - definemap result command:<[command]> entity:<[entity]> state:<[state]>
  - flag server dcutscene_modelengine.last_state:<[result]>
  - if <[entity].is_spawned||false>:
    - flag <[entity]> dcutscene_modelengine.last_state:<[result]>

modelengine_tint:
  type: task
  debug: true
  definitions: entity|tint
  script:
  - define command "modelengine tint <[entity].uuid> <[tint]>"
  - execute as_server <[command]> silent
  - definemap result command:<[command]> entity:<[entity]> tint:<[tint]>
  - flag server dcutscene_modelengine.last_tint:<[result]>
  - if <[entity].is_spawned||false>:
    - flag <[entity]> dcutscene_modelengine.last_tint:<[result]>

modelengine_visibility:
  type: task
  debug: true
  definitions: entity|visibility
  script:
  - define command "modelengine visibility <[entity].uuid> <[visibility]>"
  - execute as_server <[command]> silent
  - definemap result command:<[command]> entity:<[entity]> visibility:<[visibility]>
  - flag server dcutscene_modelengine.last_visibility:<[result]>
  - if <[entity].is_spawned||false>:
    - flag <[entity]> dcutscene_modelengine.last_visibility:<[result]>

modelengine_bone:
  type: task
  debug: true
  definitions: entity|args
  script:
  - define command "modelengine bone <[entity].uuid> <[args]>"
  - execute as_server <[command]> silent
  - definemap result command:<[command]> entity:<[entity]> args:<[args]>
  - flag server dcutscene_modelengine.last_bone:<[result]>
  - if <[entity].is_spawned||false>:
    - flag <[entity]> dcutscene_modelengine.last_bone:<[result]>

modelengine_limb:
  type: task
  debug: true
  definitions: entity|args
  script:
  - define command "modelengine limb <[entity].uuid> <[args]>"
  - execute as_server <[command]> silent
  - definemap result command:<[command]> entity:<[entity]> args:<[args]>
  - flag server dcutscene_modelengine.last_limb:<[result]>
  - if <[entity].is_spawned||false>:
    - flag <[entity]> dcutscene_modelengine.last_limb:<[result]>

modelengine_hitbox:
  type: task
  debug: true
  definitions: entity|args
  script:
  - define command "modelengine hitbox <[entity].uuid> <[args]>"
  - execute as_server <[command]> silent
  - definemap result command:<[command]> entity:<[entity]> args:<[args]>
  - flag server dcutscene_modelengine.last_hitbox:<[result]>
  - if <[entity].is_spawned||false>:
    - flag <[entity]> dcutscene_modelengine.last_hitbox:<[result]>

modelengine_mount:
  type: task
  debug: true
  definitions: entity|mount
  script:
  - define command "modelengine mount <[entity].uuid> <[mount]>"
  - execute as_server <[command]> silent
  - definemap result command:<[command]> entity:<[entity]> mount:<[mount]>
  - flag server dcutscene_modelengine.last_mount:<[result]>
  - if <[entity].is_spawned||false>:
    - flag <[entity]> dcutscene_modelengine.last_mount:<[result]>
