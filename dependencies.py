#!/opt/virtualenvs/python3/bin/python

import sys
import argparse
import requests
import json
import xml.etree.ElementTree as ET

def removeDependencies():
		mytree = ET.parse('pom.xml')

		dependencies = mytree.find("dependencies")
		for child in list(dependencies):
			dependencies.remove(child)

		mytree.write('pom.xml')	

def addDependency(group, artifact, version):
		xml = f"<dependency><groupId>{group}</groupId><artifactId>{artifact}</artifactId><version>{version}</version></dependency>\r"

		mytree = ET.parse('pom.xml')

		# get new dependency
		dependency = ET.fromstring(xml)

		dependencies = mytree.find("dependencies")
		dependencies.append(dependency)
		
		mytree.write('pom.xml')

def getPackage(artifact, group=None, version=None, repo=None):

	if repo == "local":
		addDependency(group, artifact, version)
		return

	url = "https://search.maven.org/solrsearch/select"
	q = []

	if group != None:
		q.append(f"g:\"{group}\"")
	if artifact != None:	
		q.append(f"a:\"{artifact}\"")
	if version != None:	
		q.append(f"v:\"{version}\"")

	delim = " AND "
	q_string = delim.join(q)

	url = f"https://search.maven.org/solrsearch/select?q={q_string}&rows=1&wt=json"

	print(url)

	payload={}
	headers = {}

	data = requests.request("GET", url, headers=headers, data=payload)
	response = data.json() 

	if (response["response"]["numFound"] >= 1):
		group = response["response"]["docs"][0]["g"]
		artifact = response["response"]["docs"][0]["a"]
		version = version or response["response"]["docs"][0]["latestVersion"]

		addDependency(group, artifact, version)

		return

	else:
		print("No Package Found")
		return None

if __name__ == "__main__":
		args = len(sys.argv)

		parser = argparse.ArgumentParser()
		parser.add_argument("action", help="Action")
		parser.add_argument("-a", "--artifact", help="Artifact")
		parser.add_argument("-g", "--group", help="Group")
		parser.add_argument("-v", "--version", help="Version")
		parser.add_argument("-r", "--repo", help="Repo")
		args = parser.parse_args()	

		if args.action == "clean":
			# Keep things Clean, Remove the Dependency Node
			removeDependencies()
		elif args.action == "add":
			# Don't Check Maven if this is Local
			if args.repo == "local":
				getPackage(args.artifact,args.group,args.version,args.repo)
			else:
				getPackage(args.artifact,args.group,args.version)
