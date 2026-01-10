#
# ModelEngine 4 wrapper tasks
#

modelengine_spawn_model:
  type: task
  debug: true
  definitions: model_name|location|tracking_range|viewer|cutscene_id
  script:
  - define location <[location].as[location]>
  - define tracking_range <[tracking_range].if_null[256]>
  - define viewer <[viewer].if_null[server]>
  - define viewer_target <[viewer].as_list.first||<[viewer]>>
  - define viewer_uuid <[viewer_target].uuid||null>
  - define cutscene_id <[cutscene_id].if_null[editor]>
  - define scan_radius 16
  - define max_attempts 10
  - define wait_ticks 2t
  - if <[viewer_uuid]> == null:
    - debug "modelengine_spawn_model: missing viewer UUID."
    - stop
  - define command "cs_me4 spawn <[viewer_uuid]> <[cutscene_id]> <[model_name]> <[location].x> <[location].y> <[location].z> <[location].yaw> <[location].pitch>"
  - define before_entities <[location].find_entities.within[<[scan_radius]>]||<list>>
  - execute as_server <[command]> silent
  - define spawned_entity null
  - repeat <[max_attempts]>:
    - wait <[wait_ticks]>
    - define after_entities <[location].find_entities.within[<[scan_radius]>]||<list>>
    - define new_entities <[after_entities].exclude[<[before_entities]>]>
    - define model_entities <[new_entities].filter[<[parse_value].flag[modelengine_model_id].equals_case_sensitive[<[model_name]>]>]>
    - define spawned_entity <[model_entities].first||<[new_entities].first||null>
    - if <[spawned_entity].is_null.not>:
      - repeat stop
  - define fallback_entity <[viewer].if_null[server].flag[dcutscene_modelengine.last_spawn.entity]||<server.flag[dcutscene_modelengine.last_spawn.entity]>>
  - if <[spawned_entity].is_null||false> && <[fallback_entity].is_spawned||false> && <[fallback_entity].flag[modelengine_model_id].equals_case_sensitive[<[model_name]>]>:
    - define spawned_entity <[fallback_entity]>
  - if <[spawned_entity].is_null||false>:
    - debug "modelengine_spawn_model: failed to resolve spawned entity for <[model_name]> near <[location]>. No new entity found and last_spawn flag missing or invalid."
  - if <[spawned_entity].is_spawned||false>:
    - flag <[spawned_entity]> modelengine_model_id:<[model_name]>
  - definemap result command:<[command]> model:<[model_name]> location:<[location]> viewer:<[viewer]> entity:<[spawned_entity]>
  - flag server dcutscene_modelengine.last_spawn:<[result]>
  - if <[viewer].is_player||false>:
    - flag <[viewer]> dcutscene_modelengine.last_spawn:<[result]>
  - determine <list[<[spawned_entity]>]>

modelengine_animate:
  type: task
  debug: true
  definitions: entity|animation|instance_id
  script:
  - define viewer <[entity].flag[dcutscene_model_owner]||<player||null>>
  - define viewer_uuid <[viewer].uuid||null>
  - define cutscene_id <[entity].flag[dcutscene_scene_uuid]||editor>
  - if <[viewer_uuid]> == null:
    - debug "modelengine_animate: missing viewer UUID."
    - stop
  - define command "cs_me4 anim_play <[viewer_uuid]> <[cutscene_id]> <[animation]>"
  - execute as_server <[command]> silent
  - definemap result command:<[command]> entity:<[entity]> animation:<[animation]>
  - flag server dcutscene_modelengine.last_animation_play:<[result]>
  - if <[entity].is_spawned||false>:
    - flag <[entity]> dcutscene_modelengine.last_animation_play:<[result]>

modelengine_end_animation:
  type: task
  debug: true
  definitions: entity|instance_id
  script:
  - define viewer <[entity].flag[dcutscene_model_owner]||<player||null>>
  - define viewer_uuid <[viewer].uuid||null>
  - define cutscene_id <[entity].flag[dcutscene_scene_uuid]||editor>
  - if <[viewer_uuid]> == null:
    - debug "modelengine_end_animation: missing viewer UUID."
    - stop
  - define animation_name <[entity].flag[dcutscene_modelengine_animation.name]||null>
  - if <[animation_name]> != null:
    - define command "cs_me4 anim_stop <[viewer_uuid]> <[cutscene_id]> <[animation_name]>"
  - else:
    - define command "cs_me4 anim_stop <[viewer_uuid]> <[cutscene_id]>"
  - execute as_server <[command]> silent
  - definemap result command:<[command]> entity:<[entity]>
  - flag server dcutscene_modelengine.last_animation_stop:<[result]>
  - if <[entity].is_spawned||false>:
    - flag <[entity]> dcutscene_modelengine.last_animation_stop:<[result]>

modelengine_delete:
  type: task
  debug: true
  definitions: entity|instance_id
  script:
  - define viewer <[entity].flag[dcutscene_model_owner]||<player||null>>
  - define viewer_uuid <[viewer].uuid||null>
  - define cutscene_id <[entity].flag[dcutscene_scene_uuid]||editor>
  - if <[viewer_uuid]> == null:
    - debug "modelengine_delete: missing viewer UUID."
    - stop
  - define command "cs_me4 remove <[viewer_uuid]> <[cutscene_id]>"
  - execute as_server <[command]> silent
  - definemap result command:<[command]> entity:<[entity]>
  - flag server dcutscene_modelengine.last_delete:<[result]>
  - if <[entity].is_spawned||false>:
    - flag <[entity]> dcutscene_modelengine.last_delete:<[result]>

modelengine_reset_model_position:
  type: task
  debug: false
  definitions: entity|instance_id
  script:
  - define viewer <[entity].flag[dcutscene_model_owner]||<player||null>>
  - define viewer_uuid <[viewer].uuid||null>
  - define cutscene_id <[entity].flag[dcutscene_scene_uuid]||editor>
  - if <[viewer_uuid]> == null:
    - debug "modelengine_reset_model_position: missing viewer UUID."
    - stop
  - define location <[entity].location>
  - define command "cs_me4 move <[viewer_uuid]> <[cutscene_id]> <[location].x> <[location].y> <[location].z> <[location].yaw> <[location].pitch>"
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
  - define command "cs_me4 skill <[entity].uuid> <[skill]>"
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
  - define command "cs_me4 state <[entity].uuid> <[state]>"
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
  - define command "cs_me4 tint <[entity].uuid> <[tint]>"
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
  - define command "cs_me4 visibility <[entity].uuid> <[visibility]>"
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
  - define command "cs_me4 bone <[entity].uuid> <[args]>"
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
  - define command "cs_me4 limb <[entity].uuid> <[args]>"
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
  - define command "cs_me4 hitbox <[entity].uuid> <[args]>"
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
  - define command "cs_me4 mount <[entity].uuid> <[mount]>"
  - execute as_server <[command]> silent
  - definemap result command:<[command]> entity:<[entity]> mount:<[mount]>
  - flag server dcutscene_modelengine.last_mount:<[result]>
  - if <[entity].is_spawned||false>:
    - flag <[entity]> dcutscene_modelengine.last_mount:<[result]>
