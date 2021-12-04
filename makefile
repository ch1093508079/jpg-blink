
test: src/MS2Frame.class
	echo 20 > config/t.NoLine
	mkdir -p video
	java src.MS2Frame sample-input | ffmpeg -r `cat config/r.NoLine` -i pipe:0 -vcodec libx264 -pix_fmt yuv420p -acodec aac -y -ss 0 -t `cat config/t.NoLine` "video/`date`.mp4"
#ffmpeg的选项 -vcodec libx264 -pix_fmt yuv420p 能让输出视频兼容大部分系统默认播放器

sample: src/MS2Frame.class
	echo 10 > config/t.NoLine
	mkdir -p video
	java src.MS2Frame sample-input

src/MS2Frame.class: src/MS2Frame.java
	javac -encoding UTF-8 src/MS2Frame.java

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
