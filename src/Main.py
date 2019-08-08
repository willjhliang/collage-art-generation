
from PIL import Image
import sys
import numpy as np
import queue
import os
import matplotlib.pyplot as plt

LIM = 30
MAX_SIZE = 5000
IMG_CNT = 5640

dx = [0, 1, 0, -1]
dy = [-1, 0, 1, 0]


def diff(u, v):  # takes in rgb
    return abs(u[0] - v[0]) + abs(u[1] - v[1]) + abs(u[2] - v[2])


def getPic(avg):
    closest = 0
    for i in range(IMG_CNT):
        if diff(avgs[i], avg) < diff(avgs[closest], avg):
            closest = i
    return closest


def bfs(start):

    pixels = []
    avg = [0, 0, 0]

    q = queue.Queue(0)
    q.put(start)
    size = 0
    while not q.empty() and size < MAX_SIZE:
        cur = q.get()
        if visited[cur[0], cur[1]]:
            continue
        sys.stdout.write("\r" + str(size))
        #sys.stdout.write("\r" + str(cur))
        size += 1
        pixels.append(cur)
        visited[cur[0]][cur[1]] = True
        for i in range(3):
            avg[i] += ref.getpixel((cur[0], cur[1]))[i]
        for i in range(4):
            next = (cur[0] + dx[i], cur[1] + dy[i])
            if next[0] < 0 or next[0] >= width or next[1] < 0 or next[1] >= height:
                continue
            if visited[next[0], next[1]]:
                continue
            flag = False
            for k in range(len(pixels)):
                if diff(ref.getpixel(pixels[k]), ref.getpixel(next)) > 30:
                    flag = True
            if flag:
                continue

            q.put(next)
    for i in range(3):
        avg[i] /= size
        avg[i] = int(avg[i])
    img = Image.open(os.path.join(compPath, os.listdir(compPath)[getPic(avg)]))
    for coord in pixels:
        pos = (coord[0] - start[0], coord[1] - start[1])
        if pos[0] < 0 or pos[0] >= img.size[0] or pos[1] < 0 or pos[1] >= img.size[1]:
            visited[pos[0], pos[1]] = False
            continue
        collage.putpixel((coord[0], coord[1]), img.getpixel((pos[0], pos[1])))
        sections.putpixel((coord[0], coord[1]), (avg[0], avg[1], avg[2]))


#read input
#database/banded_0013.jpg
refPath = input()
compPath = "database"
f = open("averages.txt", "r")

try:
    print("successfully read in input")
    ref = Image.open(refPath)
    ref.load()
except IOError:
    print("Loading failed")
    sys.exit(1)
width = ref.size[0]
height = ref.size[1]

visited = np.zeros((width, height), dtype=bool)
avgs = np.zeros((IMG_CNT + 1, 3))
for i in range(IMG_CNT):
    avgs[i] = np.fromstring(f.readline(), dtype=int, sep=" ")

#generation
print("generating collage")
collage = Image.new("RGB", (width, height), color="white")
sections = Image.new("RGB", (width, height), color="white")


for i in range(width):
    for j in range(height):
        if visited[i, j]:
            continue
        print("\n" + str(i) + " " + str(j))
        bfs((i, j))


f.close()

collage.show()
sections.show()
ref.show()
