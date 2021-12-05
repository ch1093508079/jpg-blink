default: sample
	echo visit sample to understand how it works

config-test: 
	echo 20 > config/t.head-1
	echo 0  > config/f-out-debug.head-1

test: all-class config-test
	mkdir -p video
	java src.MS2Frame sample-input | ffmpeg -r `head -1 config/r.head-1` -i pipe:0 -vcodec libx264 -pix_fmt yuv420p -acodec aac -y -ss 0 -t `head -1 config/t.head-1` "video/`date`.mp4"
#ffmpeg的选项 -vcodec libx264 -pix_fmt yuv420p 能让输出视频兼容大部分系统默认播放器

config-sample:
	echo 10 > config/t.head-1
	echo 1  > config/f-out-debug.head-1

sample: all-class config-sample
	java src.MS2Frame sample-input

config-product:
	echo as-long-as > config/t.head-1
	echo 0  > config/f-out-debug.head-1

product: all-class config-product
	echo TODO

all-class: src/*.java
	javac -encoding UTF-8 src/*.java

select_w_div_h: src/SelectWDivH.class
	java src.SelectWDivH ../pWarehouse/M/*/*.JPG
src/SelectWDivH.class: src/SelectWDivH.java
	javac -encoding UTF-8 src/SelectWDivH.java

clean:
	rm src/*.class

shred_f_out_debug:
	shred -v -u F_OUT_DEBUG/*.*

shred_all: clean shred_f_out_debug 
	shred -v -u --iterations=1 video/*
	shred -v -u --iterations=1 picture/*/*/*.*
	shred -v -u --iterations=1 picture/*/*.*
	shred -v -u --iterations=1 picture/*.*
