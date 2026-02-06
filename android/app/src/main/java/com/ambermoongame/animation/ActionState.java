package com.ambermoongame.animation;

/**
 * Enumeration of animation action states for SpriteAnimation.
 *
 * States:
 * - IDLE: Standing/breathing animation (5-10 frames)
 * - WALK: Walking animation (5-8 frames)
 * - RUN: Running animation (6-10 frames)
 * - SPRINT: Fast sprint animation (6-10 frames)
 * - JUMP: Single jump (5-8 frames)
 * - DOUBLE_JUMP: Double jump with flip/spin (8-12 frames)
 * - TRIPLE_JUMP: Triple jump with more dramatic spin (10-15 frames)
 * - FALL: Falling animation (5-8 frames)
 * - ATTACK: Melee attack swing (8-12 frames)
 * - FIRE: Projectile firing (8-12 frames)
 * - USE_ITEM: General item usage (6-10 frames)
 * - EAT: Eating food/potions (10-15 frames)
 * - HURT: Taking damage reaction (5-8 frames)
 * - DEAD: Death animation (10-15 frames)
 * - BLOCK: Blocking with shield (5-8 frames)
 * - CAST: Casting spell (10-15 frames)
 * - BURNING: On fire status effect (8-12 frames, looping)
 * - FROZEN: Frozen/slowed status effect (5-8 frames)
 * - POISONED: Poisoned status effect (6-10 frames)
 */
public enum ActionState {
    IDLE,
    WALK,
    RUN,
    SPRINT,
    JUMP,
    DOUBLE_JUMP,
    TRIPLE_JUMP,
    FALL,
    ATTACK,
    FIRE,
    USE_ITEM,
    EAT,
    HURT,
    DEAD,
    BLOCK,
    CAST,
    BURNING,
    FROZEN,
    POISONED
}
