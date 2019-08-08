
from PIL import Image
import os

compPath = "database"
f = open("averages.txt", "w+")

print("calculating averages")
for name in os.listdir(compPath):
    print(name)
    fullPath = os.path.join(compPath, name)
    img = Image.open(fullPath)

    comp = img.resize((1, 1))
    color = comp.getpixel((0, 0))
    f.write(str(color[0]) + " " + str(color[1]) + " " + str(color[2]) + "\n")

f.close()
