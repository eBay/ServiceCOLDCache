# Configurations

The configuration is in JSON format, the default location of configuration file is in the Service COLD cache working directory(where you unziped the distribution). You can also specifiy the path of your configuration file by adding args in startup.bat/startup.sh:

>startup.sh [-s /path/to/configfile.json]

Reference:
<table>
<thead>
<tr>
<th>Key</th><th>Type</th><th>Default</th><th>Description</th>
</tr>
</thead>
<tbody>
<tr>
<td>enableCache</td>
<td>Boolean</td>
<td>true</td>
<td>
<p>Set false to disable cache. But the cache still can turn on in runtime by <code>serviceCache.sh enableCache</code></p>
<p>Notice that when set this to false the Service COLD cache will still keep running and forward requests/responses, just stops cache the response</p>
</td>
</tr>
<tr>
<td>proxyPort</td>
<td>Integer</td>
<td>32876</td>
<td><p>The port Service COLD cache used</p></td>
</tr>
<tr>
<td>maxCount</td>
<td>Integer</td>
<td>5000</td>
<td><p>Cache item max count.</p></td>
</tr>
<tr>
<td>cacheDir</td>
<td>String</td>
<td>.proxycache</td>
<td><p>The folder where cache data persisted. 
</p></td>
</tr>
<tr>
<td>timeToLive</td>
<td>Integer</td>
<td>259200 (3 days)</td>
<td><p>Millisecond before a cache item can be marked as <i>out of date</i></p></td>
</tr>
<tr>
<td>exculdes</td>
<td>JSON Array</td>
<td><code>[]</code></td>
<td>
<p>Which request should be excluded from caching.</p>
<p>
<strong>Notice that, for current version, if configuration "includes" specified, the "excludes" will be ignored</strong>
</p>
<p>e.g.
<pre><code>
"excludes" : <strong style="color:red;">[</strong>
    {"uri": "http://*.ebay.com/*", method : "get"},
    {"uri": "*.pdf"}
<strong style="color:red;">]</strong>
</pre></code>
</p>
<p>The element of array is a pattern object: </p>
<p>
<pre><code>{
    "uri": "requestURI",
    "method": "httpMethod"
}</code></pre></p>
<p>
The <code><i>requestURI</i></code> supports wildcard match
</p>
<p>
The <code><i>httpMethod</i></code> is standard http method like 'post', 'get'. Default value is '*' means any method
</p>
<p>
Examples:
<pre><code>{ 
    "uri": "*",
}</code></pre>All requests
<pre><code>{ 
    "uri": "http://www.ebay.com/service/*",
    "method": "post"
}</code></pre>All post requests under uri <i>http://www.ebay.com/service/</i>
<pre><code>{ 
    "uri": "*.html",
    "method": "*"
}</code></pre>Any requests by any methods for html file
</p>
</td>
</tr>
<tr>
<td>includes</td>
<td>JSON Array</td>
<td>
<pre><code>[{ "uri": "*" }]</code></pre>
</td>
<td>
<p>
Which request should be included in cache
</p>
<p>
The pattern object is the same as it in exclude. <strong>Notice that, for current version, if "includes" specified, the "excludes" will be ignored</strong>
</p>
</td>
</tr>
<tr>
<td>uriMatchOnly</td>
<td>JSON Array</td>
<td>
<code>[]</code>
</td>
<td>
<p>
Which request's <strong>body</strong> should be excluded and only match request's <strong>URI</strong> and <strong>headers</strong>.
</p>
<p>
The pattern object is the same as it in <code>exclude</code>.
</p>
</td>
</tr>
<tr>
<td>requestKeyGenerator.skipHeaders</td>
<td>JSON Array</td>
<td>
<code>["DATE"]</code>
</td>
<td>
<p>
The cache key calculation for request will ignore these headers to keep certain requests with different header values share same cache content.
</p>
</td>
</tr>
</tbody>
</table>
