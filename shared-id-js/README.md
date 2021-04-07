# sharedid-library
sharedid.org cookie setup

## Integration
```
<script src="https://sharedid.org/lib/sharedid.js"></script>
<script>
    window.sharedID = new window.SharedID();
    window.onload = () => {
        window.sharedID.id = "sharedid-value";
    }
</script>
```
