# Writing offline-first web applications with Service Worker API
- Rapasoft
-
- 2017/01/04
- JavaScript ServiceWorker


With more devices connected Internet is more and more required resource. Web applications are providing rich content and not only written information. But what if the connection is not available, shouldn't you be possible to access information that was already provided to you? Writing applications that are optimized for offline usage increases user's experience and ensures that she will use it regularly.

Are you familiar with [devdocs.io](http://devdocs.io/)? It's my "the most favorite page" last year not only because it aggregates most of the documentation I use on a regular basis, but also because it's available offline. How does one achieve that? Well, you can simple use Cache API available in modern browsers.

For example, you would like to cache the responses of requests that fetch static assets (e.g. images, styles, scripts), so that you can view them when offline. It is described in the next image:

![Using Cache](https://lh3.googleusercontent.com/U3dqxrVj5T_GdEiLLv3qW5BR1W2mqHfS8291vJSg0_Uph8zje_JOU1dKREhAGPUAvARebMBzgrM=w1389-h780-no)

In order to load the site, browser calls a `GET` request (1) that as a response (2) returns static asset, which is put in cache and also used to render the page. Once the network is not available (3), the response comes from cache (4).

Using the Cache API is, however, awkward, it would require a lot of overhead just to manage the persisting and updating cache. The solution to that is to use [Service Worker API](https://developer.mozilla.org/en/docs/Web/API/Service_Worker_API). You can read the [introductory article](https://developers.google.com/web/fundamentals/getting-started/primers/service-workers), which basically describes everything you want to know.

But let's return to our real-world-example. The frontend app will "cache" itself when `install` event is processed by service worker. Only after that it will allow the rest of the application to be processed (`activate` event).

![Service Worker registration and activation](https://lh3.googleusercontent.com/_5yfGJucO_nfVewt7kFGgrOGh0ifYvcr-Zek6YB2GkbNrobeqcY9aPiIPqo5zdRaCPAhmDttjMjgopQWsrXdb_1AqAv2NDJ-MsAzfMQOAZ0Qor9kPTmk_RCQem4Z6rIL3Iisdq5XtoQWy2bV4kBU8RhkvINxsdKSX4FWZnYznyQtwNnDpsyX0ww0P09_nurxLSUvouLsirw70EuPqgFDICUtHJ0j979Nka8w0DEdJlwDG8w3L5OFfkx9Wuy1HwxQWXG7wi_1Td6pLQTVfaQ5c1MfhHFUvf_nDgKdGj46COj1eMeFwJljBu3fmVM7K_Mr1qklqVUvNMs_hKaL708qgHvx0aLRVwv4fdw92Ao__Ft3ESnpCbYUj2DmMQmf6KzrHz3CuTNLsBFProvc7Q-ri9K2MhLFGw3jKFvxeSgq4U9CW96E2ciI6KkHE8YsGDXhwjF7wp1P7-kLAsDGNk9DGZyf5r_UMNvcsuk9CH1equE1oUoOSTMcffCgMPRUQqr3OKORORSYycRBQwwwNecI_KgQlnodKytB3qaku3tjqsa6RJAynyhO21aSWAI79FW5VB_Wnu_Qd31nxbySTwDZv8RTgtMMZXH1T3dZQIEBVCKZkJuDR7gnOQ25Vf03d9aRMc9IjzNfrFiEpF5pYfL44L8P23dFMs-USe3-0sUekDo=w1440-h583-no)

Now, we will force the application to read from the cache first and only if it is not found the resource is loaded form the internet (e.g. cache first strategy for static resources).

![Service worker read from cache ](https://lh3.googleusercontent.com/pv1xj-aRAA_I5UaI1BJXwiBCBp1jiQHa0fks0qEV9fUQ4YsuLO9K6e5d9pZIHMZRcejFVqesLNvSh4khJyf4LxgktIA76JwRqHJEApokRZmU_HqlfbGThQzUqhyEtLV_HGTekW_X9_4GFZrNtbsV_jw2Uk5k-QZGH33S6m0GFL92JAVkNQjUx2_e4c4jPuJ24jbT77bC4INOvqt3qeZVNo-rTpJnGtrkhy4NBgYZuPtB9lihZneUvgQxTxdgxlEKG3m0F1gJ8xYgYHFjl83wT_6HemSlkXhOHI0Pob5UEBkglnuXLPVnneZMaw9LAkt--L6jQVZ1oeQg25E8KJKigdFIE3l1hkc3st0bvXRBzK_-9U5kGA_z69mlLN4-jGk3xWEYc3vM_uU9AagEpR0OAhli1lfzlCWztpPhlU2U64NLFi7cOxtvIITZjckmJDy9gVPA4RYWzcruq2xt_lSD-Xm6ingXJV9r2aOwL-hMF0VJBoxssjIoofpDOaIIw04JVx8-2CHJJ9GMg3xO9KfVyHRzfcxVuMikqhz3P_H-7PC1IZQhvrWqG3osoI3YVC8IqOqpM2sZIDjJkLgfCO3cVogHlK4OmS_6m-Ik1kNoXi8bISVLrhMTpynILs--rsYZ_OckovrUURTbwCl8YzU_Wch2xJ0VY2rEwrQNzbKdtrg=w1382-h780-no)

We will implement this in an Angular 1.6 application to view images from a gallery. The service method that fetches the images from gallery has a fallback to show just a "image not found" temporary image if request is not succesful:

> /src/app/slideshow/slideshow-service.js

    ...
    getPhotos(page = 0) {
      return this.httpService
        .get(`/api/random-image/list?page=${page}`)
        .then(result => result.data)
        .then(images => images.map((image) => new Image(image.name, image.url)))
        .catch(() => [new Image('', '/img/notfound.png')]);
    }
    ...

In order to make `/img/notfound.png` available offline we need to cache it.

> /src/app/service-worker.js

    const CACHE_NAME = 'angular-test-cache';
    const STATIC_ASSETS = [
      '/',
      '/app.bundle.js',
      '/img/notfound.png',
      '/img/favicon.ico'
    ];

    self.addEventListener('install', (event) =>
      event.waitUntil(
        caches
          .open(CACHE_NAME)
          .then((cache) => {
            log(['Static assets added to cache: ', STATIC_ASSETS]);
            return cache.addAll(STATIC_ASSETS);
          })
      )
    );

Lastly, we need to intercept the `fetch` event, in order to return correct response (cached "notfound" image):

> /src/app/service-worker.js

    self.addEventListener('fetch', (event) => {
      event.respondWith(
        caches
          .match(event.request)
          .then((response) => {
            if (response) {
              log([event.request.url, 'Retrieved from cache']);
              return response;
            }
            return fetch(event.request).catch(() => log(['An error has occured']));
          })
      );
    });

 You can find the full [sources on my github page](https://github.com/rapasoft/angular-webpack/tree/43880e4d4abf51b7a76430b59b246c51826e14ef). You won't be able to run the application fully, because of missing backend logic (websocket and image-api). But feel free to experiment with it ;).

 This topic was also part of my [TechTalk at Itera](https://www.facebook.com/IteraSlovakia/?fref=ts) so you can view the [slides from the presentation](https://drive.google.com/open?id=0B-rChDQV9i87bHhFR2ZFUTVRWGc) as well.
