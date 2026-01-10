#==================== Denizen Cutscenes ModelEngine Compatibility =====================

# Internal ModelEngine 4 compatibility layer.

# Spawn a ModelEngine model and return the root entity.
dcutscene_me_spawn_model:
    type: task
    debug: false
    definitions: model_name|location|tracking_range|fake_to
    script:
    - define model_name <[model_name]||<[model]>>
    - define location <[location]||<[loc]>>
    - if <[model_name]> == null || <[location]> == null:
      - debug error "Invalid parameters supplied to dcutscene_me_spawn_model."
      - determine null
    - define tracking_range <[tracking_range].if_null[256]>
    - define fake_to <[fake_to].if_null[<server.online_players>]>
    - modelengine spawn model:<[model_name]> location:<[location]> tracking_range:<[tracking_range]> viewers:<[fake_to]> save:spawned
    - determine <entry[spawned].determination||<entry[spawned].spawned_entity||<entry[spawned].created_queue.determination.first||null>>>

# Remove a ModelEngine model.
dcutscene_me_delete_model:
    type: task
    debug: false
    definitions: entity
    script:
    - if <[entity].is_spawned||false>:
      - modelengine delete entity:<[entity]>

# Play a ModelEngine animation.
dcutscene_me_animate:
    type: task
    debug: false
    definitions: entity|animation
    script:
    - if <[entity].is_spawned||false>:
      - modelengine animate entity:<[entity]> animation:<[animation]>

# Stop the current ModelEngine animation.
dcutscene_me_stop_animation:
    type: task
    debug: false
    definitions: entity
    script:
    - if <[entity].is_spawned||false>:
      - modelengine stop_animation entity:<[entity]>

# Reset the model to its default position.
dcutscene_me_reset_position:
    type: task
    debug: false
    definitions: entity
    script:
    - if <[entity].is_spawned||false>:
      - modelengine reset_position entity:<[entity]>
