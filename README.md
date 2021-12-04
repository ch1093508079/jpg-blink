# jpg-blink
用java为图片的选区添加闪烁效果，用ffmpeg以视频输出






















## 环境配置
sudo apt install default-jdk
sudo apt install ffmpeg

## makefile
### make test
快速生成短时间视频用于验证代码功能
### make sample
用于观察 sample-input 中的输入文件的格式和选区标识方式会怎样影响输出
### make clean
清除`src/*.class`
### make shred?
擦除覆盖影视文件

## ffmpeg参数说明
### -r `cat config/r.NoLine`
把每秒帧数写在 config/r.NoLine 可在不修改代码的情况下改变闪烁速度
### -i pipe:0
从管道接收输入图片，避免将帧存到硬盘
### -vcodec libx264 -pix_fmt yuv420p
加这些选项使输出视频能兼容系统默认播放器
### -acodec aac -y
声音选项
### -ss 0 -t `cat config/t.NoLine` 
限制视频时间
