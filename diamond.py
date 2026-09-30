"""3D Diamond drawn with GL_TRIANGLES, transformed with the keyboard.

Requires:  pip install PyOpenGL PyOpenGL_accelerate pygame numpy

Controls
--------
Translate : W / S = up / down      A / D = left / right      Q / E = away / closer
Rotate    : Arrow keys = rotate around X / Y      Z / X = rotate around Z
Scale     : + / -  (or = / -) = bigger / smaller
Reset     : R
Quit      : ESC
"""
import numpy as np
import pygame
from pygame.locals import DOUBLEBUF, OPENGL, QUIT, KEYDOWN, K_ESCAPE
from OpenGL.GL import *
from OpenGL.GLU import gluPerspective

# ---- Diamond geometry -------------------------------------------------------
SIDES = 8                      # facets around the girdle
TOP_H, GIRDLE_R, BOTTOM_H = 0.5, 1.0, -1.2
CROWN_R = 0.55                 # radius of the flat table on top


def ring(radius, y):
    return [(radius * np.cos(2 * np.pi * i / SIDES), y,
             radius * np.sin(2 * np.pi * i / SIDES)) for i in range(SIDES)]


def build_diamond():
    """Return (vertices, colors) as flat float32 arrays, 3 verts per triangle."""
    table = ring(CROWN_R, TOP_H)     # top flat face ring
    girdle = ring(GIRDLE_R, 0.0)     # widest ring
    culet = (0.0, BOTTOM_H, 0.0)     # bottom tip
    center_top = (0.0, TOP_H, 0.0)

    tris = []
    for i in range(SIDES):
        j = (i + 1) % SIDES
        tris.append((center_top, table[j], table[i]))       # table (top face)
        tris.append((table[i], table[j], girdle[j]))        # crown quad, 2 tris
        tris.append((table[i], girdle[j], girdle[i]))
        tris.append((girdle[i], girdle[j], culet))          # pavilion

    verts = np.array(tris, dtype=np.float32).reshape(-1, 3)
    # One shade per triangle so every facet is visible (diamond-like blues)
    colors = np.empty_like(verts)
    for t in range(len(tris)):
        shade = 0.45 + 0.55 * ((t * 37) % 11) / 10.0
        colors[3 * t:3 * t + 3] = (0.4 * shade, 0.8 * shade, 1.0 * shade)
    return verts, colors


# ---- Transform state --------------------------------------------------------
def initial_state():
    return {"pos": [0.0, 0.0, -6.0], "rot": [20.0, 30.0, 0.0], "scale": 1.0}


def handle_keys(state, keys, dt):
    move, spin, zoom = 3.0 * dt, 90.0 * dt, 1.0 + 1.5 * dt
    p, r = state["pos"], state["rot"]
    if keys[pygame.K_a]: p[0] -= move
    if keys[pygame.K_d]: p[0] += move
    if keys[pygame.K_w]: p[1] += move
    if keys[pygame.K_s]: p[1] -= move
    if keys[pygame.K_q]: p[2] -= move
    if keys[pygame.K_e]: p[2] += move
    if keys[pygame.K_UP]: r[0] -= spin
    if keys[pygame.K_DOWN]: r[0] += spin
    if keys[pygame.K_LEFT]: r[1] -= spin
    if keys[pygame.K_RIGHT]: r[1] += spin
    if keys[pygame.K_z]: r[2] -= spin
    if keys[pygame.K_x]: r[2] += spin
    if keys[pygame.K_EQUALS] or keys[pygame.K_PLUS] or keys[pygame.K_KP_PLUS]:
        state["scale"] *= zoom
    if keys[pygame.K_MINUS] or keys[pygame.K_KP_MINUS]:
        state["scale"] /= zoom
    state["scale"] = min(max(state["scale"], 0.1), 5.0)


def main():
    pygame.init()
    w, h = 800, 600
    pygame.display.set_mode((w, h), DOUBLEBUF | OPENGL)
    pygame.display.set_caption("3D Diamond - GL_TRIANGLES")

    glEnable(GL_DEPTH_TEST)
    glClearColor(0.05, 0.05, 0.1, 1.0)
    glMatrixMode(GL_PROJECTION)
    gluPerspective(45, w / h, 0.1, 50.0)
    glMatrixMode(GL_MODELVIEW)

    # Build geometry once and draw via vertex arrays (efficient: 1 draw call)
    verts, colors = build_diamond()
    glEnableClientState(GL_VERTEX_ARRAY)
    glEnableClientState(GL_COLOR_ARRAY)
    glVertexPointer(3, GL_FLOAT, 0, verts)
    glColorPointer(3, GL_FLOAT, 0, colors)

    state = initial_state()
    clock = pygame.time.Clock()
    running = True
    while running:
        dt = clock.tick(60) / 1000.0
        for e in pygame.event.get():
            if e.type == QUIT or (e.type == KEYDOWN and e.key == K_ESCAPE):
                running = False
            elif e.type == KEYDOWN and e.key == pygame.K_r:
                state = initial_state()
        handle_keys(state, pygame.key.get_pressed(), dt)

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT)
        glLoadIdentity()
        glTranslatef(*state["pos"])                 # translation
        glRotatef(state["rot"][0], 1, 0, 0)         # rotation
        glRotatef(state["rot"][1], 0, 1, 0)
        glRotatef(state["rot"][2], 0, 0, 1)
        s = state["scale"]
        glScalef(s, s, s)                           # scaling
        glDrawArrays(GL_TRIANGLES, 0, len(verts))   # the diamond
        pygame.display.flip()

    pygame.quit()


if __name__ == "__main__":
    main()
