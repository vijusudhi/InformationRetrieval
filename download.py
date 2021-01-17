"""
-------------------------
Download Wikipedia Topics
-------------------------
Modified by: Los Azules

This script downloads the dataset to the path "curr_path/dataset".

Usage: 
    download.py [-TOPICS_PATH] [-NUM_OF_TOPICS]
    [-TOPICS_PATH] Path of the file Wikipedia_topics
    [-NUM_OF_TOPICS] Number of topics. Pass an integer value

Note:
    - Wikepedia topics having at least one image file will be downloaded.
    - Topics with special characters "\"!" are not downloaded.
    - Topics will not be overwritten if they already exist.

--------------------------------------------------------------------------
"""

print(__doc__)

import requests
import re
import sys
import os

num = len(sys.argv)
if num < 3:
    print("[ERR] Enter all arguments as per the usage.")
    exit(0)
    
topics_path = str(sys.argv[1])

try:
    file = open(topics_path, "r", encoding="utf8")
except:
    print("[ERR] Input file can not be read")
    exit(0)
    
try:
    num_of_topics = int(sys.argv[2])
except:
    print("[ERR] Number of topics is not an integer")
    exit(0)
    
curr_path = os.getcwd()
print ("[INF] Current working directory is", curr_path)
print("[INF] Topics path:", topics_path)
print("[INF] Number of topics:", num_of_topics)
print("--------------------------------------------------------------------------")

lines = file.readlines()
special_characters = "\"!"

ind = 1
for i in lines:
    if (ind-1) != num_of_topics:
        #this is the title we will search
        topic = re.sub("\"", "", i)
        topic = topic.strip()
        
        if (any(c in special_characters for c in topic)):
            print("[INF] Skipping", topic)
            continue
        else:
            print("[INF] Trying to crawl", topic)


        #this is the config for to get the first introduction of a title
        text_config = {
            'action': 'query',
            'format': 'json',
            'titles': topic,
            'prop': 'extracts',
            'exintro': True,
            'explaintext': True,
        }
        text_response = requests.get('https://en.wikipedia.org/w/api.php',params=text_config).json()
        
        
        try:
            text_page = next(iter(text_response['query']['pages'].values()))
            ex = text_page['extract']
            pid = text_page['pageid']
            # # print(ex)
        except KeyError:
            print("[ERR] No 'extract' field. Skipping topic.")
            continue
            
        if text_page['extract'] != '':
            #this is the config to get the images that are in the topic
            #we use this to count the number of images
            num_image_config = {
                'action': 'parse',
                'pageid': text_page['pageid'],
                'format': 'json',
                'prop': 'images'
            }
            num_image_response = requests.get('https://en.wikipedia.org/w/api.php',params=num_image_config).json()



            #now that we havae the number of images in the page, we ask for the images that are in the page with the title
            image_config = {
                'action': 'query',
                'format': 'json',
                'titles': topic,
                'prop': 'images',
                'imlimit': len(num_image_response['parse']['images'])
            }
            image_response = requests.get('https://en.wikipedia.org/w/api.php',params=image_config).json()
            image_page = next(iter(image_response['query']['pages'].values()))    
            
            try:
                im = image_page['images']
                # # print(ex)
            except KeyError:
                print("[ERR] No 'images' field. Skipping topic.")
                continue
                
                
            filename_pattern = re.compile(".*\.(?:jpe?g|gif|png|JPE?G|GIF|PNG)")
            imCount = 0
            for i in range(len(image_page['images'])):
                url_config = {
                    'action': 'query',
                    'format': 'json',
                    'titles': image_page['images'][i]['title'],
                    'prop': 'imageinfo',
                    'iiprop': 'url'
                }
                url_response = requests.get('https://en.wikipedia.org/w/api.php',params=url_config).json()
                url_page = next(iter(url_response['query']['pages'].values()))
                #print(url_page['imageinfo'][0]['url'])
                if (filename_pattern.search(url_page['imageinfo'][0]['url'])):
                    imCount += 1
                    
            if imCount != 0:
                try:
                    os.mkdir(curr_path + "/dataset")
                except OSError:
                    print ("[ERR] Creation of the directory %s failed" % (curr_path + "\dataset"))
                
                path = curr_path+"\dataset\Topic"+str(ind)
                ind += 1
                try:
                    os.mkdir(path)
                except OSError:
                    print ("[ERR] Creation of the directory %s failed" % path)
                else:
                    print ("[INF] Successfully crawled to directory %s " % path)
            else:
                print("[ERR] No images found. Skipping topic.")
                continue
                
            
            topic = topic.strip()
            file1 = open(path + "/" + topic+".txt","w", encoding="utf8")#write mode 
            file1.write(text_page['extract']) 
            file1.close()

            #and we  write the image files one by one in the currect directory
            #we also dont write the svg files, since as they are mostly the logos
            #modily the filename_pattern regex for to accept the proper files
            # print("writing files")
            filename_pattern = re.compile(".*\.(?:jpe?g|gif|png|JPE?G|GIF|PNG)")
            for i in range(len(image_page['images'])):
                url_config = {
                    'action': 'query',
                    'format': 'json',
                    'titles': image_page['images'][i]['title'],
                    'prop': 'imageinfo',
                    'iiprop': 'url'
                }
                url_response = requests.get('https://en.wikipedia.org/w/api.php',params=url_config).json()
                url_page = next(iter(url_response['query']['pages'].values()))
                #print(url_page['imageinfo'][0]['url'])
                if(filename_pattern.search(url_page['imageinfo'][0]['url'])):
                    # print("writing image "+url_page['imageinfo'][0]['url'].rsplit("/",1)[1])
                    with open(path + "/" + url_page['imageinfo'][0]['url'].rsplit("/",1)[1], 'wb') as handle:
                        response = requests.get(url_page['imageinfo'][0]['url'], stream=True)

                        if not response.ok:
                            print (response)

                        for block in response.iter_content(1024):
                            if not block:
                                break

                            handle.write(block)

#************************************references*******************************************************************
#https://www.mediawiki.org/wiki/API:Parsing_wikitext
#https://www.mediawiki.org/wiki/Extension:TextExtracts#Caveats
#https://stackoverflow.com/questions/58337581/find-image-by-filename-in-wikimedia-commons
#https://en.wikipedia.org/w/api.php?action=query&titles=File:Albert_Einstein_Head.jpg&prop=imageinfo&iiprop=url

#https://stackoverflow.com/questions/24474288/how-to-obtain-a-list-of-titles-of-all-wikipedia-articles
#for all titles
#https://dumps.wikimedia.org/enwiki/latest/enwiki-latest-all-titles-in-ns0.gz
#https://en.wikipedia.org/w/api.php?action=parse&pageid=252735&prop=images