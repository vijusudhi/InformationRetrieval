import requests
import re
# import the os module
import os

# detect the current working directory and print it
curr_path = os.getcwd()
print ("The current working directory is %s" % curr_path)


#the program extracts text and image data for a given topic
#and writes them in the CURRENT DIRECTORY, relative to the location the py script is invoked from
#make sure you set it properly, in case you dont want the default one


file = open("Wikipedia_topics/Wikipedia_topics.txt", "r", encoding="utf8")
lines = file.readlines()

special_characters = "\"!"

NUM_OF_TOPICS = 5

ind = 1
for i in lines:
    if ind != NUM_OF_TOPICS:
        #this is the title we will search
        topic = re.sub("\"", "", i)
        topic = topic.strip()
        
        if (any(c in special_characters for c in topic)):
            print("Skipping", topic)
            continue
        else:
            print("Crawling", topic)


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
            print("Key error")
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
                print("Key error images")
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
                path = curr_path+"/Topic"+str(ind)
                ind += 1
                try:
                    os.mkdir(path)
                except OSError:
                    print ("Creation of the directory %s failed" % path)
                else:
                    print ("Successfully created the directory %s " % path)
            else:
                print("Im count 0")
                continue
                
            
            topic = topic.strip()
            file1 = open(path + "/" + topic+".txt","w", encoding="utf8")#write mode 
            file1.write(text_page['extract']) 
            file1.close()

            #and we  write the image files one by one in the currect directory
            #we also dont write the svg files, since as they are mostly the logos
            #modily the filename_pattern regex for to accept the proper files
            print("writing files")
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
                    print("writing image "+url_page['imageinfo'][0]['url'].rsplit("/",1)[1])
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