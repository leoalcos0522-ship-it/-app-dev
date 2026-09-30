"""04 Laboratory Exercise - Geometric Transformations Using Python.

A cube drawn with 12 GL_TRIANGLES, transformed with the keyboard.

Requires:  pip install PyOpenGL pygame

Controls
--------
Translate : A / D = left / right     W / S = up / down     Q / E = away / closer
Rotate    : I / K (or Up / Down) = around X     J / L (or Left / Right) = around Y
            U / O = around Z
Scale     : + / - = bigger / smaller
Spin      : SPACE = start / stop automatic rotation (on at start)
Quit      : ESC
"""
import pygame
from pygame.locals import DOUBLEBUF, OPENGL
from OpenGL.GL import *
from OpenGL.GLU import gluPerspective

# Vertex table from the exercise (index = vertex number)
VERTICES = (
    (1, 1, 1), (1, 1, -1), (1, -1, -1), (1, -1, 1),
    (-1, 1, 1), (-1, -1, -1), (-1, -1, 1), (-1, 1, -1),
)

# Each face = one color = a pair of triangles (6 faces x 2 = 12 triangles)
FACES = (
    ((1, 0, 0), ((0, 1, 2), (0, 2, 3))),   # right  (x = 1)
    ((0, 1, 0), ((4, 7, 5), (4, 5, 6))),   # left   (x = -1)
    ((0, 0, 1), ((0, 1, 7), (0, 7, 4))),   # top    (y = 1)
    ((1, 1, 0), ((3, 2, 5), (3, 5, 6))),   # bottom (y = -1)
    ((1, 0, 1), ((0, 3, 6), (0, 6, 4))),   # front  (z = 1)
    ((0, 1, 1), ((1, 2, 5), (1, 5, 7))),   # back   (z = -1)
)

# key -> transformation applied to the current matrix
STEP, ANGLE = 0.5, 15
ACTIONS = {
    pygame.K_a: lambda: glTranslatef(-STEP, 0, 0),
    pygame.K_d: lambda: glTranslatef(STEP, 0, 0),
    pygame.K_w: lambda: glTranslatef(0, STEP, 0),
    pygame.K_s: lambda: glTranslatef(0, -STEP, 0),
    pygame.K_q: lambda: glTranslatef(0, 0, -STEP),
    pygame.K_e: lambda: glTranslatef(0, 0, STEP),
    pygame.K_i: lambda: glRotatef(-ANGLE, 1, 0, 0),
    pygame.K_k: lambda: glRotatef(ANGLE, 1, 0, 0),
    pygame.K_j: lambda: glRotatef(-ANGLE, 0, 1, 0),
    pygame.K_l: lambda: glRotatef(ANGLE, 0, 1, 0),
    pygame.K_u: lambda: glRotatef(-ANGLE, 0, 0, 1),
    pygame.K_o: lambda: glRotatef(ANGLE, 0, 0, 1),
    pygame.K_UP: lambda: glRotatef(-ANGLE, 1, 0, 0),
    pygame.K_DOWN: lambda: glRotatef(ANGLE, 1, 0, 0),
    pygame.K_LEFT: lambda: glRotatef(-ANGLE, 0, 1, 0),
    pygame.K_RIGHT: lambda: glRotatef(ANGLE, 0, 1, 0),
    pygame.K_EQUALS: lambda: glScalef(1.1, 1.1, 1.1),
    pygame.K_PLUS: lambda: glScalef(1.1, 1.1, 1.1),
    pygame.K_MINUS: lambda: glScalef(0.9, 0.9, 0.9),
}


def draw_cube():
    glBegin(GL_TRIANGLES)
    for color, triangles in FACES:
        glColor3f(*color)
        for triangle in triangles:
            for v in triangle:
                glVertex3f(*VERTICES[v])
    glEnd()


def main():
    pygame.init()
    display = (800, 600)
    pygame.display.set_mode(display, DOUBLEBUF | OPENGL)
    pygame.display.set_caption("04 Lab 1")

    glEnable(GL_DEPTH_TEST)
    gluPerspective(45, display[0] / display[1], 0.1, 50.0)
    glTranslatef(0, 0, -6)
    glRotatef(25, 1, 1, 0)
    glScalef(0.6, 0.6, 0.6)          # decrease the size of the cube

    spinning = True
    while True:
        for event in pygame.event.get():
            if event.type == pygame.QUIT or (
                    event.type == pygame.KEYDOWN and event.key == pygame.K_ESCAPE):
                pygame.quit()
                return
            if event.type == pygame.KEYDOWN:
                if event.key == pygame.K_SPACE:
                    spinning = not spinning
                elif event.key in ACTIONS:
                    ACTIONS[event.key]()

        if spinning:
            glRotatef(1, 1, 1, 0)        # continuous rotation

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT)
        draw_cube()
        pygame.display.flip()
        pygame.time.wait(10)


if __name__ == "__main__":
    main()
