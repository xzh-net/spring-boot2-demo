# Jsoup HTML 解析器

官网地址：https://www.kawaks.org/

1. 获取游戏列表，生成cps1、cps2、neogeo的文件列表txt
	
2. 执行下载html批处理

   ```
   # linux下由于换行符Unix格式问题执行失败的处理办法
   sed -i 's/\r$//' shell.sh
   sed -i 's/\r$//' cps1-172.txt
   ```

   执行成功后，会在当前路径下生成roms文件夹，包含每个游戏的html文件

3. 通过解析本地html文件，扫描出每个rom的下载地址和缩略图下载地址

4. 通过批处理下载缩略图和rom文件

5. 图片格式转换，将sshots格式为bmp

6. 图片质量（可选）

