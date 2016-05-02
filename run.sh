# docker-machine start default
# docker-machine env default
# eval '$(docker-machine env default)'

docker build -t myubuntu -f Dockerfile .
docker build -t rmi/data_volume -f DockerVolume .
docker build -t rmi/server -f DockerServer .
docker build -t rmi/client -f DockerClient .
docker create -v /data --name data_volume rmi/data_volume
docker run -d --volumes-from data_volume --name rmiserver --hostname="server" rmi/server /bin/bash -c "javac *.java; java ServerService"
docker run -d --volumes-from data_volume --name rmiclient --link rmiserver rmi/client /bin/bash -c "make test; javac *.java; java ClientTest 19999"
docker logs -f rmiclient
