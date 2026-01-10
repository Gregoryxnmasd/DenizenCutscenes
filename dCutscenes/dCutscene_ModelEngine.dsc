#==================== Denizen Cutscenes ModelEngine Compatibility =====================

# Internal ModelEngine 4 compatibility layer.

# Spawn a ModelEngine model and return the root entity.
dcutscene_me_spawn_model:
    type: task
    debug: true
    definitions: model_name|location|tracking_range|fake_to|cutscene_id
    script:
    - define model_name <[model_name]||<[model]>>
    - define location <[location]||<[loc]>>
    - if <[model_name]> == null || <[location]> == null:
      - debug error "Invalid parameters supplied to dcutscene_me_spawn_model."
      - determine null
    - define tracking_range <[tracking_range].if_null[256]>
    - define fake_to <[fake_to].if_null[<server.online_players>]>
    - define cutscene_id <[cutscene_id].if_null[editor]>
    - define modelengine_spawn <script[modelengine_spawn_model]||<script[modelengine_spawn]||null>>
    - if <[modelengine_spawn]> == null:
      - debug error "Could not find ModelEngine spawn script in dcutscene_me_spawn_model"
      - stop
    - run <[modelengine_spawn]> def.model_name:<[model_name]> def.location:<[location]> def.tracking_range:<[tracking_range]> def.viewer:<[fake_to]> def.fake_to:<[fake_to]> def.cutscene_id:<[cutscene_id]> save:spawned
    - define root <entry[spawned].determination.first||<entry[spawned].determination||<entry[spawned].spawned_entity||<entry[spawned].created_queue.determination.first||null>>>>
    - if <[root]> == null:
      - define fallback_entity <server.flag[dcutscene_modelengine.last_spawn.entity]>
      - if <[fake_to].is_player||false>:
        - define fallback_entity <[fake_to].flag[dcutscene_modelengine.last_spawn.entity]||<[fallback_entity]>>
      - if <[fallback_entity].is_spawned||false> && <[fallback_entity].flag[modelengine_model_id].equals_case_sensitive[<[model_name]>]>:
        - define root <[fallback_entity]>
    - if <[root]> != null:
      - flag <[root]> modelengine_model_id:<[model_name]>
    - determine <[root]>

# Remove a ModelEngine model.
dcutscene_me_delete_model:
    type: task
    debug: true
    definitions: entity
    script:
    - if <[entity].is_spawned||false>:
      - run modelengine_delete def:<[entity]>

# Play a ModelEngine animation.
dcutscene_me_animate:
    type: task
    debug: true
    definitions: entity|animation
    script:
    - if <[entity].is_spawned||false>:
      - run modelengine_animate def.entity:<[entity]> def.animation:<[animation]>

# Stop the current ModelEngine animation.
dcutscene_me_stop_animation:
    type: task
    debug: true
    definitions: entity
    script:
    - if <[entity].is_spawned||false>:
      - run modelengine_end_animation def:<[entity]>

# Reset the model to its default position.
dcutscene_me_reset_position:
    type: task
    debug: true
    definitions: entity
    script:
    - if <[entity].is_spawned||false>:
      - run modelengine_reset_model_position def:<[entity]>

# Use a ModelEngine skill on a model.
dcutscene_me_skill:
    type: task
    debug: true
    definitions: entity|skill
    script:
    - if <[entity].is_spawned||false>:
      - run modelengine_skill def.entity:<[entity]> def.skill:<[skill]>

# Change ModelEngine state.
dcutscene_me_state:
    type: task
    debug: true
    definitions: entity|state
    script:
    - if <[entity].is_spawned||false>:
      - run modelengine_state def.entity:<[entity]> def.state:<[state]>

# Apply ModelEngine tint.
dcutscene_me_tint:
    type: task
    debug: true
    definitions: entity|tint
    script:
    - if <[entity].is_spawned||false>:
      - run modelengine_tint def.entity:<[entity]> def.tint:<[tint]>

# Toggle ModelEngine visibility.
dcutscene_me_visibility:
    type: task
    debug: true
    definitions: entity|visibility
    script:
    - if <[entity].is_spawned||false>:
      - run modelengine_visibility def.entity:<[entity]> def.visibility:<[visibility]>

# Apply ModelEngine bone adjustments.
dcutscene_me_bone:
    type: task
    debug: true
    definitions: entity|args
    script:
    - if <[entity].is_spawned||false>:
      - run modelengine_bone def.entity:<[entity]> def.args:<[args]>

# Apply ModelEngine limb adjustments.
dcutscene_me_limb:
    type: task
    debug: true
    definitions: entity|args
    script:
    - if <[entity].is_spawned||false>:
      - run modelengine_limb def.entity:<[entity]> def.args:<[args]>

# Apply ModelEngine hitbox adjustments.
dcutscene_me_hitbox:
    type: task
    debug: true
    definitions: entity|args
    script:
    - if <[entity].is_spawned||false>:
      - run modelengine_hitbox def.entity:<[entity]> def.args:<[args]>

# Apply ModelEngine mount settings.
dcutscene_me_mount:
    type: task
    debug: true
    definitions: entity|mount
    script:
    - if <[entity].is_spawned||false>:
      - run modelengine_mount def.entity:<[entity]> def.mount:<[mount]>
