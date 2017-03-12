f = open("ImagesURLs.txt","w") #opens file with name of "ImagesURLs15.txt"

for j in range(0,160):
    for i in range(0,60):
        f.write("http://goo.gl/uoBq8r")
        f.write(" ")
    f.write("\n")
f.close()