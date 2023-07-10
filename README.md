# Azuki Love Island
![ali-banner](https://github.com/heyskylark/azuki-love-island/assets/5904744/3edaf593-8fcc-455e-912a-f150163daf2f)

**Azuki Love Island (Season 1 & 2), love in paradise**

This is the codebase for Azuki Love Island (Season 1 & 2). A game show based on the Azuki brand and Love Island series, where community members compete to be voted as the number one couple by the community.

This website is broken into two parts, being the `frontend` which is programmed in React with TypeScript, and the `backend` which is a dockerized Spring Boot Kotlin service. Misc dependencies that were reuqired for the website to run include, access to the Azuki smart contract on Ethereum, Cloudinary (CDN), and MongoDb.

## Status

Currently this version of Azuki Love Island is now out of commision and a completely new version will be built from the ground up for Season 3, with the help of the community. The reason for this is that there are a lot better frameworks to use outside of React (such as NextJs), and a full Kotlin web service running on AWS is probably not necessary for such short events (each season lasting 2 weeks max). I wanted to release this code now that it is out of commission so parts of it can hopefully be used as a guide for the next version that will be created.

## Frontend

The frontend is a classic create React app, using TypeScript. The FE ran on Vercel and was driven by the Twitter API and the Kotlin backend service. To run the React app, use:

```
cd frontend
npm install
npm start:dev
```

## Backend

The backend service uses Kotlin and Spring Boot and runs on a Tomcat instance within a docker container. You can run the service locally, as there is a docker-compose file that will start up the web server and a local instance of MongoDb.

To run locally:
- Install or run docker. For OSX, I use [Docker Desktop](https://www.docker.com/products/docker-desktop/)

```
./gradlew build
docker-compose up -d
```

To view the running docker instance and its logs:
```
docker ls
docker exec -it api bash
tail -f logs/api.log
```

## Disclaimer

It looks like I had a lot of un-pushed code changes left over (maybe from changes I was making on the fly for season 2?). So I'm not 100% sure on how this will run right now, so I would mainly encourage to use this as a reference.

## Questions or Contact

Twitter: [@heyskylark](https://twitter.com/heyskylark)
