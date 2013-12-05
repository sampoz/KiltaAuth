from django.http import HttpResponse
from django.views.decorators.csrf import csrf_exempt
from rest_framework.renderers import JSONRenderer
from rest_framework.parsers import JSONParser
from CardUsers.models import CardUsers
from CardUsers.serializers import CardUsersSerializer

class JSONResponse(HttpResponse):
	"""
	An HttpResponse that renders its content into JSON.
	"""
	def __init__(self, data, **kwargs):
		content = JSONRenderer().render(data)
		kwargs['content_type'] = 'application/json'
		super(JSONResponse, self).__init__(content, **kwargs)
@csrf_exempt
def CardUsers_list(request):
	if request.method == 'GET':
		cardUsers = CardUsers.objects.all()
		serializer = CardUsersSerializer(cardUsers, many=True)
		return JSONResponse(serializer.data)

	elif request.method == 'POST':
		data = JSONParser().parse(request)
		serializer = CardUsersSerializer(data=data)
		if serializer.is_valid():
			serializer.save()
			return JSONResponse(serializer.data, status=201)
		else:
			return JSONResponse(serializer.errors, status=400)
@csrf_exempt
def CardUsers_detail(request, pk):
	try:
		cardUsers = CardUsers.objects.get(cardId=pk)
	except CardUsers.DoesNotExist:
		return HttpResponse(status=404)
	if request.method == 'GET':
		serializer = CardUsersSerializer(cardUsers)
		return JSONResponse(serializer.data)
	elif request.method == 'PUT':
		data = JSONParser().parse(request)
		serializer = CardUsersSerializer(cardUsers, data=data)
		if serializer.is_valid():
			serializer.save()
			return JSONResponse(serializer.data)
		else:
			return JSONResponse(serializer.errors, status=400)
	if request.method == 'DELETE':
		cardUsers.delete()
		return HttpResponse(status=204)
@csrf_exempt
def CardUsers_card(request, card):
	try:
		cardUsers = CardUsers.objects.get(cardId=card)
	except CardUsers.DoesNotExist:
		return HttpResponse(status=404)
	if request.method == 'GET':
		serializer = CardUsersSerializer(cardUsers)
		return JSONResponse(serializer.data)
	elif request.method == 'PUT':
		data = JSONParser().parse(request)
		serializer = CardUsersSerializer(cardUsers, data=data)
		if serializer.is_valid():
			serializer.save()
			return JSONResponse(serializer.data)
		else:
			return JSONResponse(serializer.errors, status=400)
	if request.method == 'DELETE':
		cardUsers.delete()
		return HttpResponse(status=204)

